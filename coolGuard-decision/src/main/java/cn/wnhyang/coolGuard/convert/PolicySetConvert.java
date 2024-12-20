package cn.wnhyang.coolGuard.convert;

import cn.wnhyang.coolGuard.context.PolicyContext;
import cn.wnhyang.coolGuard.entity.PolicySet;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.vo.PolicySetVO;
import cn.wnhyang.coolGuard.vo.create.PolicySetCreateVO;
import cn.wnhyang.coolGuard.vo.update.PolicySetUpdateVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 策略集表
 *
 * @author wnhyang
 * @since 2024/04/04
 */
@Mapper
public interface PolicySetConvert {

    PolicySetConvert INSTANCE = Mappers.getMapper(PolicySetConvert.class);

    PolicySet convert(PolicySetCreateVO createVO);

    PolicySet convert(PolicySetUpdateVO updateVO);

    PolicySetVO convert(PolicySet po);

    PageResult<PolicySetVO> convert(PageResult<PolicySet> pageResult);

    List<PolicySetVO> convert(List<PolicySet> list);

    PolicyContext.PolicySetCtx convert2Ctx(PolicySet policySet);
}
