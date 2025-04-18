package cn.wnhyang.coolguard.system.entity;

import cn.wnhyang.coolguard.mybatis.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 参数表
 *
 * @author wnhyang
 * @since 2025/01/07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_param")
public class ParamDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 参数id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 参数标签
     */
    @TableField("name")
    private String name;

    /**
     * 参数值
     */
    @TableField("code")
    private String code;

    /**
     * 参数类型
     */
    @TableField("type")
    private String type;

    /**
     * 参数数据
     */
    @TableField("data")
    private String data;

    /**
     * 标准
     */
    @TableField("standard")
    private Boolean standard;

    /**
     * 描述
     */
    @TableField("description")
    private String description;
}
