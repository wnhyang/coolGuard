package cn.wnhyang.coolguard.decision.vo.result;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wnhyang
 * @date 2024/4/8
 **/
@Getter
public class RuleResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 954520164753579904L;

    /**
     * 规则名称
     */
    private final String name;

    /**
     * 规则编码
     */
    private final String code;

    public RuleResult(String name, String code, String express) {
        this.name = name;
        this.code = code;
        this.express = express;
    }

    /**
     * 处置名称
     */
    @Setter
    private String disposalName;

    /**
     * 处置编码
     */
    @Setter
    private String disposalCode;

    /**
     *
     */
    @Setter
    private String express;

}
