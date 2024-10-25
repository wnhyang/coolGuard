package cn.wnhyang.coolGuard.vo.create;

import lombok.Data;

import java.io.Serializable;

/**
 * 策略集版本表
 *
 * @author wnhyang
 * @since 2024/08/29
 */
@Data
public class PolicySetVersionCreateVO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 策略集id
     */
    private Long policySetId;

    /**
     * 策略id
     */
    private Long policyId;

    /**
     * 策略状态
     */
    private Integer version;
}
