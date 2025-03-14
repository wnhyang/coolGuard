package cn.wnhyang.coolguard.decision.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.wnhyang.coolguard.decision.constant.ValueType;
import cn.wnhyang.coolguard.decision.context.DecisionContextHolder;
import cn.wnhyang.coolguard.decision.context.FieldContext;
import cn.wnhyang.coolguard.decision.context.IndicatorContext;
import cn.wnhyang.coolguard.decision.entity.Cond;
import cn.wnhyang.coolguard.decision.enums.CondType;
import cn.wnhyang.coolguard.decision.enums.FieldType;
import cn.wnhyang.coolguard.decision.enums.LogicType;
import cn.wnhyang.coolguard.decision.service.CondService;
import cn.wnhyang.coolguard.decision.service.ListDataService;
import cn.wnhyang.coolguard.decision.util.FunUtil;
import com.yomahub.liteflow.annotation.LiteflowComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则条件表 服务实现类
 *
 * @author wnhyang
 * @since 2024/04/04
 */
@Slf4j
@Service
@LiteflowComponent
@RequiredArgsConstructor
public class CondServiceImpl implements CondService {

    private final ListDataService listDataService;

    @Override
    public boolean cond(Cond cond) {
        log.info("条件:{}", cond);
        // 如果有子节点，则递归计算子节点的结果
        if (CollUtil.isNotEmpty(cond.getChildren())) {
            List<Cond> children = cond.getChildren();
            return switch (cond.getRelation().toUpperCase()) {
                case "AND" -> children.stream().allMatch(this::condLeaf);
                case "OR" -> children.stream().anyMatch(this::condLeaf);
                default -> throw new IllegalArgumentException("Unsupported relation: " + cond.getRelation());
            };
        }

        // 处理叶子节点
        return condLeaf(cond);
    }

    public boolean condLeaf(Cond cond) {
        if (cond == null) {
            return false;
        }
        if (cond.getLeftValue() == null) {
            return true;
        }

        boolean b = false;

        try {
            FieldContext fieldContext = DecisionContextHolder.getFieldContext();
            IndicatorContext indicatorContext = DecisionContextHolder.getIndicatorContext();
            CondType condType = CondType.getByType(cond.getType());
            LogicType byType = LogicType.getByType(cond.getLogicType());
            switch (condType) {
                // 普通条件，适用指标、规则
                case NORMAL -> {
                    // 获取条件字段
                    String fieldCode = cond.getLeftValue();
                    FieldType fieldType = FieldType.getByFieldCode(fieldCode);

                    String expectValue = cond.getRightValue();
                    if (ValueType.CONTEXT.equals(cond.getRightType())) {
                        expectValue = fieldContext.getData2String(expectValue);
                    }

                    if (fieldType == null || byType == null) {
                        return false;
                    }
                    String strValue = fieldContext.getData2String(fieldCode);
                    log.info("普通条件，字段类型:{}, 字段值:{}, 操作:{}, 期望值:{}", fieldType, strValue, byType, expectValue);
                    b = switch (fieldType) {
                        case STRING ->
                                FunUtil.INSTANCE.stringLogicOp.apply(fieldContext.getData(fieldCode, String.class), byType, expectValue);
                        case NUMBER ->
                                FunUtil.INSTANCE.integerLogicOp.apply(fieldContext.getData(fieldCode, Integer.class), byType, Integer.parseInt(expectValue));
                        case FLOAT ->
                                FunUtil.INSTANCE.doubleLogicOp.apply(fieldContext.getData(fieldCode, Double.class), byType, Double.parseDouble(expectValue));
                        case DATE ->
                                FunUtil.INSTANCE.dateLogicOp.apply(fieldContext.getData(fieldCode, LocalDateTime.class), byType, LocalDateTimeUtil.parse(expectValue, DatePattern.NORM_DATETIME_FORMATTER));
                        case ENUM ->
                                FunUtil.INSTANCE.enumLogicOp.apply(fieldContext.getData(fieldCode, String.class), byType, expectValue);
                        case BOOLEAN ->
                                FunUtil.INSTANCE.booleanLogicOp.apply(fieldContext.getData(fieldCode, Boolean.class), byType, Boolean.parseBoolean(expectValue));
                    };
                }
                case ZB -> {
                    log.info("指标条件");
                    String indicatorCode = cond.getLeftValue();
                    String iType = indicatorContext.getIndicatorReturnType(indicatorCode);
                    FieldType fieldType = FieldType.getByType(iType);

                    String expectValue = cond.getRightValue();
                    if (ValueType.CONTEXT.equals(cond.getRightType())) {
                        expectValue = fieldContext.getData2String(expectValue);
                    }

                    if (fieldType == null || byType == null) {
                        return false;
                    }

                    String strValue = indicatorContext.getIndicatorValue2String(indicatorCode);
                    log.info("指标条件，字段类型:{}, 字段值:{}, 操作:{}, 期望值:{}", fieldType, strValue, byType, expectValue);
                    b = switch (fieldType) {
                        case STRING ->
                                FunUtil.INSTANCE.stringLogicOp.apply(indicatorContext.getIndicatorValue(indicatorCode, String.class), byType, expectValue);
                        case NUMBER ->
                                FunUtil.INSTANCE.integerLogicOp.apply(indicatorContext.getIndicatorValue(indicatorCode, Integer.class), byType, Integer.parseInt(expectValue));
                        case FLOAT ->
                                FunUtil.INSTANCE.doubleLogicOp.apply(indicatorContext.getIndicatorValue(indicatorCode, Double.class), byType, Double.parseDouble(expectValue));
                        case DATE ->
                                FunUtil.INSTANCE.dateLogicOp.apply(indicatorContext.getIndicatorValue(indicatorCode, LocalDateTime.class), byType, LocalDateTimeUtil.parse(expectValue, DatePattern.NORM_DATETIME_FORMATTER));
                        case ENUM ->
                                FunUtil.INSTANCE.enumLogicOp.apply(indicatorContext.getIndicatorValue(indicatorCode, String.class), byType, expectValue);
                        case BOOLEAN ->
                                FunUtil.INSTANCE.booleanLogicOp.apply(indicatorContext.getIndicatorValue(indicatorCode, Boolean.class), byType, Boolean.parseBoolean(expectValue));
                    };
                }
                case REGULAR -> {
                    log.info("正则条件");

                    String fieldCode = cond.getLeftValue();

                    String stringData = fieldContext.getData2String(fieldCode);
                    b = FunUtil.INSTANCE.regularLogicOp.apply(stringData, byType, cond.getRightValue());
                }
                case LIST -> {
                    log.info("名单条件");
                    String fieldCode = cond.getLeftValue();

                    String stringData = fieldContext.getData2String(fieldCode);

                    b = switch (byType) {
                        case IN -> listDataService.hasListData(cond.getRightValue(), stringData);
                        case NOT_IN -> !listDataService.hasListData(cond.getRightValue(), stringData);
                        default -> throw new IllegalStateException("Unexpected value: " + byType);
                    };
                }
                case SCRIPT -> {
                    // TODO 脚本条件
                    log.info("脚本条件");
                }
                default -> {
                    log.error("未知条件类型:{}", condType);
                }
            }
        } catch (Exception e) {
            log.error("条件:{}, 运行异常:{}", cond, e.getMessage());
        }
        return b;
    }

}
