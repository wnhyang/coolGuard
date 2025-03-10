package cn.wnhyang.coolGuard.decision.mapper;

import cn.wnhyang.coolGuard.common.pojo.PageResult;
import cn.wnhyang.coolGuard.decision.entity.PolicyVersion;
import cn.wnhyang.coolGuard.decision.vo.page.PolicyVersionPageVO;
import cn.wnhyang.coolGuard.mybatis.mapper.BaseMapperX;
import cn.wnhyang.coolGuard.mybatis.wrapper.LambdaQueryWrapperX;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 策略版本表 Mapper 接口
 *
 * @author wnhyang
 * @since 2025/02/11
 */
@Mapper
public interface PolicyVersionMapper extends BaseMapperX<PolicyVersion> {

    default PageResult<PolicyVersion> selectPage(PolicyVersionPageVO pageVO) {
        return selectPage(pageVO, new LambdaQueryWrapperX<PolicyVersion>());
    }

    default PageResult<PolicyVersion> selectPageByCode(PolicyVersionPageVO pageVO) {
        return selectPage(pageVO, new LambdaQueryWrapperX<PolicyVersion>()
                .eq(PolicyVersion::getCode, pageVO.getCode())
                .orderByDesc(PolicyVersion::getVersion));
    }

    default PolicyVersion selectLatestByCode(String code) {
        return selectOne(PolicyVersion::getCode, code, PolicyVersion::getLatest, Boolean.TRUE);
    }

    default List<PolicyVersion> selectLatestBySetCode(String setCode) {
        return selectList(new LambdaQueryWrapperX<PolicyVersion>()
                .eq(PolicyVersion::getCode, setCode)
                .eq(PolicyVersion::getLatest, Boolean.TRUE));
    }

    default List<PolicyVersion> selectLatestList() {
        return selectList(PolicyVersion::getLatest, Boolean.TRUE);
    }

    default PolicyVersion selectLatestVersion(String code) {
        return selectOne(new LambdaQueryWrapperX<PolicyVersion>()
                .eq(PolicyVersion::getCode, code)
                .orderByDesc(PolicyVersion::getVersion)
                .last("LIMIT 1"));
    }

    default PolicyVersion selectByCode(String code) {
        return selectOne(PolicyVersion::getCode, code);
    }

    default void deleteByCode(String code) {
        delete(PolicyVersion::getCode, code);
    }
}
