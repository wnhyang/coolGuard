package cn.wnhyang.coolGuard.convert;

import cn.wnhyang.coolGuard.entity.RuleScript;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.vo.RuleScriptVO;
import cn.wnhyang.coolGuard.vo.create.RuleScriptCreateVO;
import cn.wnhyang.coolGuard.vo.update.RuleScriptUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 规则脚本表
 *
 * @author wnhyang
 * @since 2024/04/03
 */
@Mapper
public interface RuleScriptConvert {

    RuleScriptConvert INSTANCE = Mappers.getMapper(RuleScriptConvert.class);

    RuleScript convert(RuleScriptCreateVO createVO);

    RuleScript convert(RuleScriptUpdateVO updateVO);

    RuleScriptVO convert(RuleScript po);

    PageResult<RuleScriptVO> convert(PageResult<RuleScript> pageResult);

}