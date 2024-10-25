package cn.wnhyang.coolGuard.vo;

import cn.wnhyang.coolGuard.entity.ConfigField;
import lombok.Data;

import java.io.Serial;

/**
 * 接入字段表
 *
 * @author wnhyang
 * @since 2024/04/04
 */
@Data
public class AccessFieldVO extends ConfigField {

    @Serial
    private static final long serialVersionUID = 7303993433636717669L;

    /**
     * 字段显示名
     */
    private String displayName;
}
