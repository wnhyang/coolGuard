package cn.wnhyang.coolguard.decision.convert;

import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.decision.context.PolicyContext;
import cn.wnhyang.coolguard.decision.dto.RuleDTO;
import cn.wnhyang.coolguard.decision.entity.Rule;
import cn.wnhyang.coolguard.decision.entity.RuleVersion;
import cn.wnhyang.coolguard.decision.vo.RuleVO;
import cn.wnhyang.coolguard.decision.vo.create.RuleCreateVO;
import cn.wnhyang.coolguard.decision.vo.update.RuleUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 规则表
 *
 * @author wnhyang
 * @since 2024/04/04
 */
@Mapper
public interface RuleConvert {

    RuleConvert INSTANCE = Mappers.getMapper(RuleConvert.class);

    Rule convert(RuleCreateVO createVO);

    Rule convert(RuleUpdateVO updateVO);

    RuleVO convert(Rule po);

    PageResult<RuleVO> convert(PageResult<Rule> pageResult);

    List<RuleVO> convert(List<Rule> list);

    PageResult<RuleVO> convert2(PageResult<RuleDTO> rulePageResult);

    List<PolicyContext.RuleCtx> convert2Ctx(List<RuleVersion> ruleVersions);
}
