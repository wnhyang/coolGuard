package cn.wnhyang.coolGuard.vo.base;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wnhyang
 * @date 2024/4/16
 **/
@Data
public class PolicySetBaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -267362598649710050L;

    /**
     * 策略集名
     */
    private String name;

    /**
     * 策略集状态
     */
    private Boolean status;

    /**
     * 描述
     */
    private String description;

}
