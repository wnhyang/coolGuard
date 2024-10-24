package cn.wnhyang.coolGuard.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 处置表
 *
 * @author wnhyang
 * @since 2024/04/03
 */
@Data
public class DisposalDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 处置编码
     */
    private String code;

    /**
     * 处置名
     */
    private String name;

    /**
     * 等级
     */
    private Integer grade;

    /**
     * 颜色
     */
    private String color;

    /**
     * 是否为标准
     */
    private Boolean standard;

    /**
     * 描述
     */
    private String description;
}
