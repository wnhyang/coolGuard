package cn.wnhyang.coolGuard.vo.page;

import cn.wnhyang.coolGuard.pojo.PageParam;
import lombok.Data;

import java.io.Serial;

/**
 * 消息模版表
 *
 * @author wnhyang
 * @since 2024/12/08
 */
@Data
public class SmsTemplatePageVO extends PageParam {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;
}
