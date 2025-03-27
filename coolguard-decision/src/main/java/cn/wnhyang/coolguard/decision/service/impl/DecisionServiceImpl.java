package cn.wnhyang.coolguard.decision.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.wnhyang.coolguard.common.util.JsonUtil;
import cn.wnhyang.coolguard.decision.constant.AccessMode;
import cn.wnhyang.coolguard.decision.constant.FieldCode;
import cn.wnhyang.coolguard.decision.constant.KafkaConstant;
import cn.wnhyang.coolguard.decision.context.DecisionContextHolder;
import cn.wnhyang.coolguard.decision.context.EventContext;
import cn.wnhyang.coolguard.decision.context.FieldContext;
import cn.wnhyang.coolguard.decision.entity.Access;
import cn.wnhyang.coolguard.decision.entity.ParamConfig;
import cn.wnhyang.coolguard.decision.kafka.producer.CommonProducer;
import cn.wnhyang.coolguard.decision.mapper.AccessMapper;
import cn.wnhyang.coolguard.decision.service.*;
import cn.wnhyang.coolguard.decision.vo.AccessVO;
import cn.wnhyang.coolguard.decision.vo.EventData;
import cn.wnhyang.coolguard.decision.vo.result.DecisionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author wnhyang
 * @date 2025/3/8
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {

    private final AsyncTaskExecutor asyncExecutor;

    private final CommonProducer commonProducer;

    private final AccessMapper accessMapper;

    private final AccessService accessService;

    private final FieldService fieldService;

    private final IndicatorService indicatorService;

    private final PolicySetService policySetService;

    private DecisionResult access(String code, Map<String, String> params, String mode) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("接入");
        log.info("服务名：{}, 入参：{}", code, params);

        DecisionResult decisionResult = new DecisionResult();
        try {
            stopWatch.stop();

            stopWatch.start("accessDB");
            // 根据接入名称获取接入
            AccessVO accessVO = accessService.getAccessByCode(code);
            stopWatch.stop();

            stopWatch.start("字段解析");
            // 事件开始
            DecisionContextHolder.setEventContext(new EventContext());

            // 字段解析
            FieldContext fieldContext = fieldService.fieldParse(accessVO.getInputFieldList(), params);
            DecisionContextHolder.setFieldContext(fieldContext);
            stopWatch.stop();
            // 指标计算
            stopWatch.start("指标计算");
            indicatorService.indicatorCompute();
            stopWatch.stop();

            if (!AccessMode.ASYNC.equals(mode)) {
                stopWatch.start("策略集");
                // 执行策略集
                policySetService.policySet();
                stopWatch.stop();
            }
            stopWatch.start("结果");
            // 策略结果
            decisionResult.setPolicySetResult(DecisionContextHolder.getEventContext().getPolicySetResult());
            // 设置出参
            Map<String, Object> outFields = new HashMap<>(16);
            outFields.put(FieldCode.SEQ_ID, fieldContext.getData(FieldCode.SEQ_ID, String.class));
            if (CollUtil.isNotEmpty(accessVO.getOutputFieldList())) {
                for (ParamConfig outputField : accessVO.getOutputFieldList()) {
                    outFields.put(outputField.getParamName(), fieldContext.getData2String(outputField.getFieldCode()));
                }
            }
            decisionResult.setOutFields(outFields);
            stopWatch.stop();

            // 将上下文拼在一块，将此任务丢到线程中执行
            stopWatch.start("ES");
            EventData eventData = new EventData();
            eventData.setZd(fieldContext);
            eventData.setZb(DecisionContextHolder.getIndicatorContext().convert());
            eventData.setPolicySetResult(DecisionContextHolder.getEventContext().getPolicySetResult());
            try {
                commonProducer.send(KafkaConstant.EVENT_ES_DATA, JsonUtil.toJsonString(eventData));
            } catch (Exception e) {
                log.error("esData error", e);
            }
            stopWatch.stop();
        } finally {
            DecisionContextHolder.removeAll();
        }
        log.info(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        return decisionResult;
    }

    @Override
    public DecisionResult testRisk(String code, Map<String, String> params) {
        DecisionResult result = access(code, params, AccessMode.TEST);
        accessMapper.updateByCode(new Access().setCode(code).setTestParams(params));
        return result;
    }

    @Override
    public DecisionResult syncRisk(String code, Map<String, String> params) {
        return access(code, params, AccessMode.SYNC);
    }

    @Override
    public DecisionResult asyncRisk(String code, Map<String, String> params) {

        DecisionResult decisionResult = new DecisionResult();
        try {
            return asyncExecutor.submit(() ->
                    access(code, params, AccessMode.ASYNC)).get(100, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return decisionResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
