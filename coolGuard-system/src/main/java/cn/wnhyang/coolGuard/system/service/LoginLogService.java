package cn.wnhyang.coolGuard.system.service;


import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.system.dto.LoginLogCreateDTO;
import cn.wnhyang.coolGuard.system.entity.LoginLogPO;
import cn.wnhyang.coolGuard.system.vo.loginlog.LoginLogPageVO;
import jakarta.validation.Valid;

/**
 * 系统访问记录
 *
 * @author wnhyang
 * @since 2023/07/25
 */
public interface LoginLogService {

    /**
     * 创建登录日志
     *
     * @param reqDTO 日志信息
     */
    void createLoginLog(@Valid LoginLogCreateDTO reqDTO);

    /**
     * 分页查询登录日志
     *
     * @param reqVO 分页请求
     * @return 登录日志分页
     */
    PageResult<LoginLogPO> getLoginLogPage(LoginLogPageVO reqVO);
}