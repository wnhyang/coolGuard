package cn.wnhyang.coolguard.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wnhyang
 * @date 2023/7/25
 **/
@Data
public class LoginLogCreateDTO implements Serializable {

    private static final long serialVersionUID = -3025051336842878235L;

    @NotNull(message = "日志类型不能为空")
    private Integer loginType;

    private Long userId;

    @NotNull(message = "用户类型不能为空")
    private Integer userType;

    @NotBlank(message = "用户账号不能为空")
    @Size(max = 30, message = "用户账号长度不能超过30个字符")
    private String account;

    @NotNull(message = "登录结果不能为空")
    private Integer result;

    @NotBlank(message = "用户 IP 不能为空")
    private String userIp;

    private String userAgent;
}
