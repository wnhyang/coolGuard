package cn.wnhyang.coolGuard.indicator;

import cn.wnhyang.coolGuard.constant.FieldName;
import cn.wnhyang.coolGuard.context.IndicatorContext;
import cn.wnhyang.coolGuard.enums.IndicatorType;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author wnhyang
 * @date 2024/5/10
 **/
@Component
public class MaxIndicator extends AbstractIndicator {

    public MaxIndicator(RedissonClient redissonClient) {
        super(redissonClient);
    }

    @Override
    public IndicatorType getType() {
        return IndicatorType.MAX;
    }

    @Override
    public Object getResult0(IndicatorContext.IndicatorCtx indicator, RScoredSortedSet<String> set) {
        double max = 0.0;
        for (String item : set) {
            String[] split = item.split("-");
            if (split.length >= 2) {
                double v = Double.parseDouble(split[1]);
                if (v > max) {
                    max = v;
                }
            }
        }

        return max;
    }

    @Override
    public void addEvent(IndicatorContext.IndicatorCtx indicator, long eventTime, Map<String, Object> eventDetail, RScoredSortedSet<String> set) {
        set.add(eventTime, eventDetail.get(FieldName.seqId) + "-" + eventDetail.get(indicator.getCalcField()));

    }
}