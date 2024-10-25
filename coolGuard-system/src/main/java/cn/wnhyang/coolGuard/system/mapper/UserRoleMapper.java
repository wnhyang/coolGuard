package cn.wnhyang.coolGuard.system.mapper;

import cn.wnhyang.coolGuard.mybatis.BaseMapperX;
import cn.wnhyang.coolGuard.system.entity.UserRolePO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 用户和角色关联表
 *
 * @author wnhyang
 * @since 2023/05/14
 */
@Mapper
public interface UserRoleMapper extends BaseMapperX<UserRolePO> {

    default List<UserRolePO> selectListByUserId(Collection<Long> userIds) {
        return selectList(UserRolePO::getUserId, userIds);
    }

    default void deleteByUserId(Long userId) {
        delete(Wrappers.lambdaUpdate(UserRolePO.class).eq(UserRolePO::getUserId, userId));
    }

    default List<UserRolePO> selectListByUserId(Long userId) {
        return selectList(UserRolePO::getUserId, userId);
    }

    default void deleteListByUserIdAndRoleIdIds(Long userId, Collection<Long> roleIds) {
        delete(new LambdaQueryWrapper<UserRolePO>()
                .eq(UserRolePO::getUserId, userId)
                .in(UserRolePO::getRoleId, roleIds));
    }

    default Long selectCountByRoleId(Long roleId) {
        return selectCount(UserRolePO::getRoleId, roleId);
    }
}
