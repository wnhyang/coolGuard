package cn.wnhyang.coolGuard.mapper;

import cn.wnhyang.coolGuard.constant.RedisKey;
import cn.wnhyang.coolGuard.entity.FieldGroup;
import cn.wnhyang.coolGuard.mybatis.BaseMapperX;
import cn.wnhyang.coolGuard.mybatis.LambdaQueryWrapperX;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.vo.page.FieldGroupPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.cache.annotation.Cacheable;

/**
 * 字段分组表 Mapper 接口
 *
 * @author wnhyang
 * @since 2024/03/14
 */
@Mapper
public interface FieldGroupMapper extends BaseMapperX<FieldGroup> {

    default PageResult<FieldGroup> selectPage(FieldGroupPageVO pageVO) {
        return selectPage(pageVO, new LambdaQueryWrapperX<FieldGroup>()
                .likeIfPresent(FieldGroup::getDisplayName, pageVO.getDisplayName())
                .likeIfPresent(FieldGroup::getName, pageVO.getName()));
    }

    @Cacheable(cacheNames = RedisKey.FIELD_GROUP + "::na", key = "#name", unless = "#result == null")
    default FieldGroup selectByName(String name) {
        return selectOne(FieldGroup::getName, name);
    }
}