package cn.wnhyang.coolguard.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.wnhyang.coolguard.common.constant.UserConstants;
import cn.wnhyang.coolguard.common.util.CollectionUtils;
import cn.wnhyang.coolguard.system.entity.MenuDO;
import cn.wnhyang.coolguard.system.entity.RoleMenuDO;
import cn.wnhyang.coolguard.system.entity.UserRoleDO;
import cn.wnhyang.coolguard.system.mapper.MenuMapper;
import cn.wnhyang.coolguard.system.mapper.RoleMenuMapper;
import cn.wnhyang.coolguard.system.mapper.UserRoleMapper;
import cn.wnhyang.coolguard.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static cn.wnhyang.coolguard.common.util.CollectionUtils.convertSet;

/**
 * 权限
 *
 * @author wnhyang
 * @date 2023/8/4
 **/
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final MenuMapper menuMapper;

    private final UserRoleMapper userRoleMapper;

    private final RoleMenuMapper roleMenuMapper;

    @Override
    public Set<Long> getRoleIdListByUserId(Collection<Long> userId) {
        if (CollUtil.isEmpty(userId)) {
            return Collections.emptySet();
        }
        return convertSet(userRoleMapper.selectListByUserId(userId), UserRoleDO::getRoleId);
    }

    @Override
    public Set<Long> getMenuIdListByRoleId(Collection<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptySet();
        }
        // 如果是管理员的情况下，获取全部菜单编号
        if (hasAnyAdministrator(roleIds)) {
            return convertSet(menuMapper.selectListOrder(), MenuDO::getId);
        }
        // 如果是非管理员的情况下，获得拥有的菜单编号
        return convertSet(roleMenuMapper.selectListByRoleId(roleIds), RoleMenuDO::getMenuId);
    }

    @Override
    public Set<String> getPermissionsByRoleIds(Collection<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptySet();
        }
        // 如果是管理员的情况下，获取全部菜单编号
        if (hasAnyAdministrator(roleIds)) {
            return Collections.singleton("*:*:*");
        }
        // 如果是非管理员的情况下，获得拥有的菜单编号
        Set<Long> menuIds = getMenuIdListByRoleId(roleIds);
        return convertSet(menuMapper.selectByIds(menuIds), MenuDO::getPermission);
    }

    private boolean hasAnyAdministrator(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return false;
        }
        return ids.stream().anyMatch(UserConstants.ADMINISTRATOR_ROLE_ID::equals);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void roleMenu(Long roleId, Set<Long> menuIds) {
        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = convertSet(roleMenuMapper.selectListByRoleId(roleId), RoleMenuDO::getMenuId);
        // 计算新增和删除的菜单编号
        Collection<Long> createMenuIds = CollUtil.subtract(menuIds, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIds);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createMenuIds)) {
            roleMenuMapper.insertBatch(CollectionUtils.convertList(createMenuIds, menuId -> {
                RoleMenuDO entity = new RoleMenuDO();
                entity.setRoleId(roleId);
                entity.setMenuId(menuId);
                return entity;
            }));
        }
        if (CollUtil.isNotEmpty(deleteMenuIds)) {
            roleMenuMapper.deleteListByRoleIdAndMenuIds(roleId, deleteMenuIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userRole(Long userId, Set<Long> roleIds) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = convertSet(userRoleMapper.selectListByUserId(userId),
                UserRoleDO::getRoleId);
        // 计算新增和删除的角色编号
        Collection<Long> createRoleIds = CollUtil.subtract(roleIds, dbRoleIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbRoleIds, roleIds);
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollectionUtil.isEmpty(createRoleIds)) {
            userRoleMapper.insertBatch(CollectionUtils.convertList(createRoleIds, roleId -> {
                UserRoleDO entity = new UserRoleDO();
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                return entity;
            }));
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            userRoleMapper.deleteListByUserIdAndRoleIdIds(userId, deleteMenuIds);
        }
    }

}
