package cn.wnhyang.coolGuard.vo.create;

import lombok.Data;

import java.io.Serializable;

/**
 * 名单集表
 *
 * @author wnhyang
 * @since 2024/05/28
 */
@Data
public class ListSetCreateVO implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 名单集名
     */
    private String name;

    /**
     * 名单集类型
     */
    private String type;

    /**
     * 名单集状态
     */
    private Boolean status;

    /**
     * 描述
     */
    private String description;
}