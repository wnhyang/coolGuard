package cn.wnhyang.coolGuard.service;

import cn.wnhyang.coolGuard.entity.RuleScript;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.vo.create.RuleScriptCreateVO;
import cn.wnhyang.coolGuard.vo.page.RuleScriptPageVO;
import cn.wnhyang.coolGuard.vo.update.RuleScriptUpdateVO;

/**
 * 规则脚本表 服务类
 *
 * @author wnhyang
 * @since 2024/04/03
 */
public interface RuleScriptService {

    /**
     * 新建
     *
     * @param createVO 新建VO
     * @return id
     */
    Long createRuleScript(RuleScriptCreateVO createVO);

    /**
     * 更新
     *
     * @param updateVO 更新VO
     */
    void updateRuleScript(RuleScriptUpdateVO updateVO);

    /**
     * 删除
     *
     * @param id id
     */
    void deleteRuleScript(Long id);

    /**
     * 查询单个
     *
     * @param id id
     * @return po
     */
    RuleScript getRuleScript(Long id);

    /**
     * 分页查询
     *
     * @param pageVO 分页VO
     * @return pageResult
     */
    PageResult<RuleScript> pageRuleScript(RuleScriptPageVO pageVO);

}