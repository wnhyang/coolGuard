package cn.wnhyang.coolGuard.convert;

import cn.wnhyang.coolGuard.context.IndicatorContext;
import cn.wnhyang.coolGuard.entity.Indicator;
import cn.wnhyang.coolGuard.entity.IndicatorVersion;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.vo.IndicatorVersionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 指标表版本表
 *
 * @author wnhyang
 * @since 2024/11/21
 */
@Mapper
public interface IndicatorVersionConvert {

    IndicatorVersionConvert INSTANCE = Mappers.getMapper(IndicatorVersionConvert.class);

    IndicatorVersionVO convert(IndicatorVersion po);

    PageResult<IndicatorVersionVO> convert(PageResult<IndicatorVersion> pageResult);

    @Mapping(target = "id", ignore = true)
    IndicatorVersion convert(Indicator indicator);

    List<IndicatorContext.IndicatorCtx> convert2Ctx(List<IndicatorVersion> indicatorVersionList);
}