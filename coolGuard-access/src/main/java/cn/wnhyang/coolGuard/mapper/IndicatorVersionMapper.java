package cn.wnhyang.coolGuard.mapper;

import cn.wnhyang.coolGuard.constant.SceneType;
import cn.wnhyang.coolGuard.entity.IndicatorVersion;
import cn.wnhyang.coolGuard.mybatis.BaseMapperX;
import cn.wnhyang.coolGuard.mybatis.LambdaQueryWrapperX;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.vo.page.IndicatorVersionPageVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 指标表历史表 Mapper 接口
 *
 * @author wnhyang
 * @since 2024/11/21
 */
@Mapper
public interface IndicatorVersionMapper extends BaseMapperX<IndicatorVersion> {

    default PageResult<IndicatorVersion> selectPage(IndicatorVersionPageVO pageVO) {
        return selectPage(pageVO, new LambdaQueryWrapperX<IndicatorVersion>()
                .eq(IndicatorVersion::getStatus, Boolean.TRUE)
                .likeIfPresent(IndicatorVersion::getName, pageVO.getName())
                .eqIfPresent(IndicatorVersion::getType, pageVO.getType())
                .eqIfPresent(IndicatorVersion::getSceneType, pageVO.getSceneType())
                .eqIfPresent(IndicatorVersion::getScenes, pageVO.getScene()));
    }

    default IndicatorVersion selectRunningByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<IndicatorVersion>()
                .eq(IndicatorVersion::getCode, code)
                .eq(IndicatorVersion::getStatus, Boolean.TRUE));
    }

    default List<IndicatorVersion> selectRunningListByScenes(String app, String policySet) {
        return selectList(new LambdaQueryWrapperX<IndicatorVersion>()
                .eq(IndicatorVersion::getStatus, Boolean.TRUE)
                .and(w -> w.eq(IndicatorVersion::getSceneType, SceneType.APP).apply("FIND_IN_SET({0}, scenes)", app))
                .or(w -> w.eq(IndicatorVersion::getSceneType, SceneType.POLICY_SET).apply("FIND_IN_SET({0}, scenes)", policySet))
        );
    }
}