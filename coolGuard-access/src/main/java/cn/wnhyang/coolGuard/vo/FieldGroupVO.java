package cn.wnhyang.coolGuard.vo;

import cn.wnhyang.coolGuard.vo.base.FieldGroupBaseVO;
import lombok.Data;

import java.io.Serial;

/**
 * @author wnhyang
 * @date 2024/3/14
 **/
@Data
public class FieldGroupVO extends FieldGroupBaseVO {

    @Serial
    private static final long serialVersionUID = -3827226919190224275L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 分组标识
     */
    private String name;

    /**
     * 是否为标准
     */
    private Boolean standard;

    /**
     * 组内字段数
     */
    private Long count;

}