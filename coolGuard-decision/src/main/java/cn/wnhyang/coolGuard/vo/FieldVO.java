package cn.wnhyang.coolGuard.vo;

import cn.wnhyang.coolGuard.vo.base.FieldBaseVO;
import lombok.Data;

import java.io.Serial;

/**
 * @author wnhyang
 * @date 2024/3/14
 **/
@Data
public class FieldVO extends FieldBaseVO {

    @Serial
    private static final long serialVersionUID = 6783204005218778510L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 是否标准字段，是：不可以删除
     */
    private Boolean standard;

    /**
     * 字段名
     */
    private String name;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 是否动态字段(0否1是)
     */
    private Boolean dynamic;
}