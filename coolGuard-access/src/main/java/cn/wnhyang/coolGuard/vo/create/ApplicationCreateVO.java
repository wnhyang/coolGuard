package cn.wnhyang.coolGuard.vo.create;

import cn.wnhyang.coolGuard.vo.base.ApplicationBaseVO;
import lombok.Data;

import java.io.Serial;

/**
 * 应用表
 *
 * @author wnhyang
 * @since 2024/04/03
 */
@Data
public class ApplicationCreateVO extends ApplicationBaseVO {

    @Serial
    private static final long serialVersionUID = -4178585550757568445L;

    /**
     * 应用名
     */
    private String name;

    /**
     * 密钥
     */
    private String secret;
}