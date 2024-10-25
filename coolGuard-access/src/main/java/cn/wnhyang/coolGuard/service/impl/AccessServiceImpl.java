package cn.wnhyang.coolGuard.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.wnhyang.coolGuard.constant.FieldName;
import cn.wnhyang.coolGuard.constant.KafkaConstant;
import cn.wnhyang.coolGuard.context.AccessRequest;
import cn.wnhyang.coolGuard.context.AccessResponse;
import cn.wnhyang.coolGuard.context.IndicatorContext;
import cn.wnhyang.coolGuard.context.PolicyContext;
import cn.wnhyang.coolGuard.convert.AccessConvert;
import cn.wnhyang.coolGuard.convert.FieldConvert;
import cn.wnhyang.coolGuard.entity.*;
import cn.wnhyang.coolGuard.kafka.producer.CommonProducer;
import cn.wnhyang.coolGuard.mapper.AccessMapper;
import cn.wnhyang.coolGuard.mapper.ChainMapper;
import cn.wnhyang.coolGuard.mapper.FieldMapper;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.service.AccessService;
import cn.wnhyang.coolGuard.service.DisposalService;
import cn.wnhyang.coolGuard.util.JsonUtils;
import cn.wnhyang.coolGuard.util.LFUtil;
import cn.wnhyang.coolGuard.vo.AccessVO;
import cn.wnhyang.coolGuard.vo.InputFieldVO;
import cn.wnhyang.coolGuard.vo.OutputFieldVO;
import cn.wnhyang.coolGuard.vo.create.AccessCreateVO;
import cn.wnhyang.coolGuard.vo.page.AccessPageVO;
import cn.wnhyang.coolGuard.vo.update.AccessUpdateVO;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import com.yomahub.liteflow.annotation.LiteflowFact;
import com.yomahub.liteflow.annotation.LiteflowMethod;
import com.yomahub.liteflow.core.FlowExecutor;
import com.yomahub.liteflow.core.NodeComponent;
import com.yomahub.liteflow.enums.LiteFlowMethodEnum;
import com.yomahub.liteflow.enums.NodeTypeEnum;
import com.yomahub.liteflow.flow.LiteflowResponse;
import com.yomahub.liteflow.flow.entity.CmpStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static cn.wnhyang.coolGuard.exception.ErrorCodes.ACCESS_NAME_EXIST;
import static cn.wnhyang.coolGuard.exception.ErrorCodes.ACCESS_NOT_EXIST;
import static cn.wnhyang.coolGuard.exception.util.ServiceExceptionUtil.exception;

/**
 * 接入表 服务实现类
 *
 * @author wnhyang
 * @since 2024/03/14
 */
@Slf4j
@Service
@LiteflowComponent
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {

    private final AccessMapper accessMapper;

    private final DisposalService disposalService;

    private final FlowExecutor flowExecutor;

    private final CommonProducer commonProducer;

    private final FieldMapper fieldMapper;

    private final ChainMapper chainMapper;

    @Override
    public AccessResponse syncRisk(String name, Map<String, String> params) {

        AccessResponse accessResponse = new AccessResponse();

        // 根据接入名称获取接入
        Access access = getAccessByName(name);

        // 设置接入
        List<InputFieldVO> inputFields = getAccessInputFieldList(access, access.getId());
        List<OutputFieldVO> outputFields = getAccessOutputFieldList(access, access.getId());

        AccessRequest accessRequest = new AccessRequest(access, params, inputFields, outputFields);
        PolicyContext policyContext = new PolicyContext();
        for (Disposal disposal : disposalService.listDisposal()) {
            policyContext.addDisposal(disposal.getId(), disposal);
        }
        IndicatorContext indicatorContext = new IndicatorContext();

        LiteflowResponse syncRisk = flowExecutor.execute2Resp(StrUtil.format(LFUtil.ACCESS_CHAIN, access.getId()), null, accessRequest, indicatorContext, policyContext, accessResponse);

        Queue<CmpStep> executeStepQueue = syncRisk.getExecuteStepQueue();
        for (CmpStep cmpStep : executeStepQueue) {
            log.info("cmpStep:{}", cmpStep.buildStringWithTime());
        }

        // 将上下文拼在一块，将此任务丢到线程中执行
        Map<String, Object> esData = new HashMap<>();
        esData.put("fields", accessRequest.getFields());
        esData.put("zbs", indicatorContext.convert());
        esData.put("result", accessResponse.getPolicySetResult());
        try {
            commonProducer.send(KafkaConstant.EVENT_ES_DATA, JsonUtils.toJsonString(esData));
        } catch (Exception e) {
            log.error("esData error", e);
        }
        return accessResponse;
    }

    @Override
    public AccessResponse asyncRisk(String name, Map<String, String> params) {
        log.info("name {}", name);

        AccessResponse accessResponse = new AccessResponse();
        return accessResponse;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAccess(AccessCreateVO createVO) {
        // 1、校验服务name唯一性
        validateForCreateOrUpdate(null, createVO.getName());
        Access access = AccessConvert.INSTANCE.convert(createVO);
        accessMapper.insert(access);
        // TODO 创建chain
        String aChain = StrUtil.format(LFUtil.ACCESS_CHAIN, access.getId());
        chainMapper.insert(new Chain().setChainName(aChain));
        return access.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccess(AccessUpdateVO updateVO) {
        Access access = AccessConvert.INSTANCE.convert(updateVO);

        accessMapper.updateById(access);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAccess(Long id) {
        validateExists(id);
        accessMapper.deleteById(id);
        chainMapper.deleteByChainName(StrUtil.format(LFUtil.ACCESS_CHAIN, id));
    }

    @Override
    public AccessVO getAccess(Long id) {
        Access access = accessMapper.selectById(id);
        return AccessConvert.INSTANCE.convert(access);
    }

    @Override
    public PageResult<AccessVO> pageAccess(AccessPageVO pageVO) {
        PageResult<Access> pageResult = accessMapper.selectPage(pageVO);
        return AccessConvert.INSTANCE.convert(pageResult);
    }

    @Override
    public Access getAccessByName(String name) {
        Access access = accessMapper.selectByName(name);
        if (access == null) {
            throw exception(ACCESS_NOT_EXIST);
        }
        return access;
    }

    @Override
    public List<ConfigField> getInputFieldList(Long id) {
        String json = accessMapper.selectInputConfig(id);
        if (StrUtil.isNotBlank(json)) {
            return JsonUtils.parseArray(json, ConfigField.class);
        }
        return List.of();
    }

    @Override
    public List<ConfigField> getOutputFieldList(Long id) {
        String json = accessMapper.selectOutputConfig(id);
        if (StrUtil.isNotBlank(json)) {
            return JsonUtils.parseArray(json, ConfigField.class);
        }
        return List.of();
    }

    @Override
    public List<InputFieldVO> getAccessInputFieldList(Access access, Long id) {
        List<ConfigField> configFieldList = access != null ?
                JsonUtils.parseArray(access.getInputConfig(), ConfigField.class) : getInputFieldList(id);

        List<InputFieldVO> inputFieldVOList = new ArrayList<>();
        if (CollUtil.isNotEmpty(configFieldList)) {
            for (ConfigField configField : configFieldList) {
                Field field = fieldMapper.selectByName(configField.getFieldName());
                if (field != null) {
                    InputFieldVO inputFieldVO = FieldConvert.INSTANCE.convert2InputFieldVO(field);
                    inputFieldVO.setParamName(configField.getParamName());
                    inputFieldVO.setRequired(configField.getRequired());
                    inputFieldVOList.add(inputFieldVO);
                }
            }
        }
        return inputFieldVOList;
    }

    @Override
    public List<OutputFieldVO> getAccessOutputFieldList(Access access, Long id) {
        List<ConfigField> configFieldList = access != null ?
                JsonUtils.parseArray(access.getOutputConfig(), ConfigField.class) : getOutputFieldList(id);

        List<OutputFieldVO> outputFieldVOList = new ArrayList<>();
        if (CollUtil.isNotEmpty(configFieldList)) {
            for (ConfigField configField : configFieldList) {
                Field field = fieldMapper.selectByName(configField.getFieldName());
                if (field != null) {
                    outputFieldVOList.add(new OutputFieldVO()
                            .setName(configField.getFieldName())
                            .setParamName(configField.getParamName()));
                }
            }
        }
        return outputFieldVOList;
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS, nodeId = LFUtil.EMPTY_COMMON_NODE, nodeType = NodeTypeEnum.COMMON, nodeName = "空组件")
    public void empty(NodeComponent bindCmp) {
        log.info("空组件");
    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS, nodeId = LFUtil.ACCESS_IN_COMMON_NODE, nodeType = NodeTypeEnum.COMMON, nodeName = "入参组件")
    public void accessIn(NodeComponent bindCmp, @LiteflowFact("params") Map<String, String> params) {
        log.info("入参：{}", params);

    }

    @LiteflowMethod(value = LiteFlowMethodEnum.PROCESS, nodeId = LFUtil.ACCESS_OUT_COMMON_NODE, nodeType = NodeTypeEnum.COMMON, nodeName = "出参组件")
    public void accessOut(NodeComponent bindCmp) {
        AccessRequest accessRequest = bindCmp.getContextBean(AccessRequest.class);
        AccessResponse accessResponse = bindCmp.getContextBean(AccessResponse.class);
        // 设置出参
        accessResponse.setOutputData(FieldName.seqId, accessRequest.getStringData(FieldName.seqId));
        List<OutputFieldVO> outputFields = accessRequest.getOutputFields();
        if (CollUtil.isNotEmpty(outputFields)) {
            for (OutputFieldVO outputField : outputFields) {
                accessResponse.setOutputData(outputField.getParamName(),
                        accessRequest.getStringData(outputField.getName()));
            }
        }
    }

    private void validateForCreateOrUpdate(Long id, String name) {
        // 校验存在
        validateExists(id);
        // 校验名唯一
        validateNameUnique(id, name);
    }

    private void validateExists(Long id) {
        if (id == null) {
            return;
        }
        Access access = accessMapper.selectById(id);
        if (access == null) {
            throw exception(ACCESS_NOT_EXIST);
        }
    }

    private void validateNameUnique(Long id, String name) {
        if (StrUtil.isBlank(name)) {
            return;
        }
        Access access = accessMapper.selectByName(name);
        if (access == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(ACCESS_NAME_EXIST);
        }
        if (!access.getId().equals(id)) {
            throw exception(ACCESS_NAME_EXIST);
        }
    }
}
