package cn.wnhyang.coolguard.decision.vo;

import cn.wnhyang.coolguard.decision.vo.result.IndicatorResult;
import cn.wnhyang.coolguard.decision.vo.result.PolicySetResult;
import lombok.Data;

import java.util.Map;

/**
 * @author wnhyang
 * @date 2025/3/16
 **/
@Data
public class EventData {

    private Map<String, Object> zd;

    private Map<String, IndicatorResult> zb;

    private PolicySetResult policySetResult;
}
