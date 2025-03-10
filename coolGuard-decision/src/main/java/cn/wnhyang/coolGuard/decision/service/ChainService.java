package cn.wnhyang.coolGuard.decision.service;

import cn.wnhyang.coolGuard.common.pojo.PageResult;
import cn.wnhyang.coolGuard.decision.entity.Chain;
import cn.wnhyang.coolGuard.decision.vo.create.ChainCreateVO;
import cn.wnhyang.coolGuard.decision.vo.page.ChainPageVO;
import cn.wnhyang.coolGuard.decision.vo.update.ChainUpdateVO;

/**
 * chain表 服务类
 *
 * @author wnhyang
 * @since 2024/04/04
 */
public interface ChainService {

    /**
     * 新建
     *
     * @param createVO 新建VO
     * @return id
     */
    Long createChain(ChainCreateVO createVO);

    /**
     * 更新
     *
     * @param updateVO 更新VO
     */
    void updateChain(ChainUpdateVO updateVO);

    /**
     * 删除
     *
     * @param id id
     */
    void deleteChain(Long id);

    /**
     * 查询单个
     *
     * @param id id
     * @return po
     */
    Chain getChain(Long id);

    /**
     * 分页查询
     *
     * @param pageVO 分页VO
     * @return pageResult
     */
    PageResult<Chain> pageChain(ChainPageVO pageVO);

}
