package cn.wnhyang.coolguard.decision.vo;

import cn.wnhyang.coolguard.decision.vo.base.PolicyBaseVO;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * 策略表
 *
 * @author wnhyang
 * @since 2024/04/04
 */
@Data
public class PolicyVO extends PolicyBaseVO {

    @Serial
    private static final long serialVersionUID = -8532588059860661308L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 策略集id
     */
    private String policySetCode;

    /**
     * 策略编码
     */
    private String code;

    /**
     * 策略模式
     */
    private String mode;

    /**
     * 规则列表
     */
    private List<RuleVO> ruleList;

    /**
     * 策略状态
     */
    private Boolean publish;

    /**
     * 最新
     */
    private Boolean latest;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 版本描述
     */
    private String versionDesc;

}
