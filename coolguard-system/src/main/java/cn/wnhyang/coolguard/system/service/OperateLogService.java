package cn.wnhyang.coolguard.system.service;


import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.system.dto.OperateLogCreateDTO;
import cn.wnhyang.coolguard.system.entity.OperateLogDO;
import cn.wnhyang.coolguard.system.vo.operatelog.OperateLogPageVO;

/**
 * 操作日志记录
 *
 * @author wnhyang
 * @since 2023/06/05
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 操作日志请求
     */
    void createOperateLog(OperateLogCreateDTO createReqDTO);

    /**
     * 分页查询操作日志
     *
     * @param reqVO 分页请求
     * @return 分页操作日志
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageVO reqVO);
}
