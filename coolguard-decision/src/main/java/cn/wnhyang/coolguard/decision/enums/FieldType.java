package cn.wnhyang.coolguard.decision.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * @author wnhyang
 * @date 2024/3/13
 **/
@AllArgsConstructor
@Getter
public enum FieldType {

    /**
     * 字符型，支持【等于、不等于、包含、不包含、前缀、非前缀、后缀、非后缀、为空、不为空】
     */
    STRING("字符串", "S", String.class),

    /**
     * 整数型，支持【等于、不等于、大于、小于、大于等于、小于等于、为空、不为空】
     */
    NUMBER("整数", "N", Integer.class),

    /**
     * 小数型，支持【等于、不等于、大于、小于、大于等于、小于等于、为空、不为空】
     */
    FLOAT("小数", "F", Double.class),

    /**
     * 日期型，支持【等于、不等于、大于、小于、大于等于、小于等于、为空、不为空】
     */
    DATE("日期", "D", LocalDateTime.class),

    /**
     * TODO 要不要保留枚举类型？
     * 枚举型，支持【等于、不等于、为空、不为空】
     */
    ENUM("枚举", "E", String.class),

    /**
     * 布尔型，支持【等于、不等于、为空、不为空】
     */
    BOOLEAN("布尔", "B", Boolean.class);

    private final String name;

    private final String type;

    private final Class<?> clazz;

    public static FieldType getByType(String type) {
        for (FieldType value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }

    public static FieldType getByFieldCode(String fieldCode) {
        if (fieldCode.length() >= 3) {
            String sub = StrUtil.sub(fieldCode, 2, 3);
            return getByType(sub);
        }
        return null;
    }

}
