package cn.wnhyang.coolguard.decision.mapper;

import cn.hutool.core.util.StrUtil;
import cn.wnhyang.coolguard.common.pojo.PageResult;
import cn.wnhyang.coolguard.decision.constant.SceneType;
import cn.wnhyang.coolguard.decision.entity.IndicatorVersion;
import cn.wnhyang.coolguard.decision.vo.page.IndicatorVersionPageVO;
import cn.wnhyang.coolguard.decision.vo.query.CvQueryVO;
import cn.wnhyang.coolguard.mybatis.mapper.BaseMapperX;
import cn.wnhyang.coolguard.mybatis.wrapper.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 指标表版本表 Mapper 接口
 *
 * @author wnhyang
 * @since 2024/11/21
 */
@Mapper
public interface IndicatorVersionMapper extends BaseMapperX<IndicatorVersion> {

    default PageResult<IndicatorVersion> selectPage(IndicatorVersionPageVO pageVO) {
        return selectPage(pageVO, new LambdaQueryWrapperX<IndicatorVersion>()
                .eq(IndicatorVersion::getLatest, Boolean.TRUE)
                .likeIfPresent(IndicatorVersion::getName, pageVO.getName())
                .eqIfPresent(IndicatorVersion::getType, pageVO.getType())
                .eqIfPresent(IndicatorVersion::getSceneType, pageVO.getSceneType())
                .isNotNull(StrUtil.isNotBlank(pageVO.getScene()), IndicatorVersion::getScenes)
                .ne(StrUtil.isNotBlank(pageVO.getScene()), IndicatorVersion::getScenes, "''")
                .apply(StrUtil.isNotBlank(pageVO.getScene()), "JSON_CONTAINS(scenes, '\"" + pageVO.getScene() + "\"')"));
    }

    default PageResult<IndicatorVersion> selectPageByCode(IndicatorVersionPageVO pageVO) {
        return selectPage(pageVO, new LambdaQueryWrapperX<IndicatorVersion>()
                .eq(IndicatorVersion::getCode, pageVO.getCode())
                .orderByDesc(IndicatorVersion::getVersion));
    }

    default IndicatorVersion selectLatestByCode(String code) {
        return selectOne(IndicatorVersion::getCode, code, IndicatorVersion::getLatest, Boolean.TRUE);
    }

    default IndicatorVersion selectLatestVersionByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<IndicatorVersion>()
                .eq(IndicatorVersion::getCode, code)
                .orderByDesc(IndicatorVersion::getVersion)
                .last("LIMIT 1"));
    }

    default List<IndicatorVersion> selectLatestList() {
        return selectList(IndicatorVersion::getLatest, Boolean.TRUE);
    }

    default List<IndicatorVersion> selectLatestListByScenes(String app, String policySet) {
        return selectList(new LambdaQueryWrapperX<IndicatorVersion>()
                .eq(IndicatorVersion::getLatest, Boolean.TRUE)
                .and(w -> w.eq(IndicatorVersion::getSceneType, SceneType.APP)
                        .isNotNull(IndicatorVersion::getScenes)
                        .ne(IndicatorVersion::getScenes, "''")
                        .apply("JSON_CONTAINS(scenes, '\"" + app + "\"')"))
                .or(w -> w.eq(IndicatorVersion::getSceneType, SceneType.POLICY_SET)
                        .isNotNull(IndicatorVersion::getScenes)
                        .ne(IndicatorVersion::getScenes, "''")
                        .apply("JSON_CONTAINS(scenes, '\"" + policySet + "\"')"))
        );
    }

    default List<IndicatorVersion> selectByCode(String code) {
        return selectList(IndicatorVersion::getCode, code);
    }

    default IndicatorVersion selectByCv(CvQueryVO queryVO) {
        return selectOne(IndicatorVersion::getCode, queryVO.getCode(), IndicatorVersion::getVersion, queryVO.getVersion());
    }
}
