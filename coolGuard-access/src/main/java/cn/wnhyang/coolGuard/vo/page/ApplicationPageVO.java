package cn.wnhyang.coolGuard.vo.page;

import cn.wnhyang.coolGuard.pojo.PageParam;
import lombok.Data;

/**
 * 应用表
 *
 * @author wnhyang
 * @since 2024/04/03
 */
@Data
public class ApplicationPageVO extends PageParam {

    private static final long serialVersionUID = 1L;

    /**
     * 显示名
     */
    private String displayName;

    /**
     * 应用名
     */
    private String name;

}