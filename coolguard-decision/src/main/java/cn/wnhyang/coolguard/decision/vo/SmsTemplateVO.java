package cn.wnhyang.coolguard.decision.vo;

import cn.wnhyang.coolguard.decision.vo.base.SmsTemplateBase;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * 消息模版表
 *
 * @author wnhyang
 * @since 2024/12/08
 */
@Data
public class SmsTemplateVO extends SmsTemplateBase {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 消息编码
     */
    private String code;

    /**
     * 消息参数
     */
    private List<String> params;
}
