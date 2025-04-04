package cn.wnhyang.coolguard.decision.service;

import cn.wnhyang.coolguard.common.entity.LabelValue;
import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.decision.entity.RuleVersion;
import cn.wnhyang.coolguard.decision.vo.RuleVersionVO;
import cn.wnhyang.coolguard.decision.vo.page.RuleVersionPageVO;
import cn.wnhyang.coolguard.decision.vo.query.CvQueryVO;

import java.util.List;

/**
 * 规则版本表 服务类
 *
 * @author wnhyang
 * @since 2024/08/29
 */
public interface RuleVersionService {

    /**
     * 查询单个
     *
     * @param id id
     * @return po
     */
    RuleVersion get(Long id);

    /**
     * 根据code查询
     *
     * @param code code
     * @return po
     */
    List<RuleVersion> getByCode(String code);

    /**
     * 分页查询
     *
     * @param pageVO 分页VO
     * @return pageResult
     */
    PageResult<RuleVersion> page(RuleVersionPageVO pageVO);

    /**
     * 根据code分页查询
     *
     * @param pageVO 分页VO
     * @return pageResult
     */
    PageResult<RuleVersionVO> pageByCode(RuleVersionPageVO pageVO);

    /**
     * 下线
     *
     * @param id id
     */
    void offline(Long id);

    /**
     * 选中
     *
     * @param id id
     */
    void chose(Long id);

    /**
     * 获取lvList
     *
     * @return lvList
     */
    List<LabelValue> getLabelValueList();

    /**
     * 根据code和version查询
     *
     * @param queryVO queryVO
     * @return vo
     */
    RuleVersionVO getByCv(CvQueryVO queryVO);
}
