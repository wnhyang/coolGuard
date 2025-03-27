package cn.wnhyang.coolguard.decision.context;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.wnhyang.coolguard.decision.entity.IndicatorVersion;
import cn.wnhyang.coolguard.decision.vo.result.IndicatorResult;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wnhyang
 * @date 2024/4/3
 **/
public class IndicatorContext {

    private final Map<String, IndicatorCtx> indicatorMap = new ConcurrentHashMap<>(32);

    public void setIndicator(IndicatorCtx indicator) {
        indicatorMap.put(indicator.getCode(), indicator);
    }

    public String getIndicatorReturnType(String code) {
        return indicatorMap.get(code).getReturnType();
    }

    public String getIndicatorValue2String(String code) {
        return indicatorMap.get(code).getValueData2String();
    }

    public <T> T getIndicatorValue(String code, Class<T> clazz) {
        return indicatorMap.get(code).getValueData(clazz);
    }

    public Map<String, IndicatorResult> convert() {
        if (CollUtil.isEmpty(indicatorMap)) {
            return Collections.emptyMap();
        }
        Map<String, IndicatorResult> resultMap = new HashMap<>(indicatorMap.size());
        for (Map.Entry<String, IndicatorCtx> entry : indicatorMap.entrySet()) {
            IndicatorCtx indicatorCtx = entry.getValue();
            resultMap.put(
                    entry.getKey(),
                    new IndicatorResult(
                            indicatorCtx.getCode(),
                            indicatorCtx.getName(),
                            indicatorCtx.getType(),
                            indicatorCtx.getVersion(),
                            indicatorCtx.getValue()
                    )
            );
        }
        return resultMap;
    }


    @Data
    public static class IndicatorCtx extends IndicatorVersion {

        @Serial
        private static final long serialVersionUID = 1355749887039607327L;

        /**
         * 指标值
         */
        private Object value;

        public String getValueData2String() {
            if (this.value == null) {
                return null;
            }
            if (this.value instanceof LocalDateTime) {
                return LocalDateTimeUtil.formatNormal(getValueData(LocalDateTime.class));
            }
            return this.value.toString();
        }

        public <T> T getValueData(Class<T> clazz) {
            return clazz.cast(this.value);
        }

    }
}
