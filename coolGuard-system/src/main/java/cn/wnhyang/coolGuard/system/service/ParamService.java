package cn.wnhyang.coolGuard.system.service;

import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.system.entity.Param;
import cn.wnhyang.coolGuard.system.vo.param.ParamCreateVO;
import cn.wnhyang.coolGuard.system.vo.param.ParamPageVO;
import cn.wnhyang.coolGuard.system.vo.param.ParamUpdateVO;

/**
 * 参数表 服务类
 *
 * @author wnhyang
 * @since 2025/01/07
 */
public interface ParamService {

    /**
     * 新建
     *
     * @param createVO 新建VO
     * @return id
     */
    Long create(ParamCreateVO createVO);

    /**
     * 更新
     *
     * @param updateVO 更新VO
     */
    void update(ParamUpdateVO updateVO);

    /**
     * 删除
     *
     * @param id id
     */
    void delete(Long id);

    /**
     * 查询单个
     *
     * @param id id
     * @return po
     */
    Param get(Long id);

    /**
     * 分页查询
     *
     * @param pageVO 分页VO
     * @return pageResult
     */
    PageResult<Param> page(ParamPageVO pageVO);

}