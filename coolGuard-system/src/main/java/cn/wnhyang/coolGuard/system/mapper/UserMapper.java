package cn.wnhyang.coolGuard.system.mapper;


import cn.wnhyang.coolGuard.mybatis.BaseMapperX;
import cn.wnhyang.coolGuard.mybatis.LambdaQueryWrapperX;
import cn.wnhyang.coolGuard.pojo.PageResult;
import cn.wnhyang.coolGuard.system.entity.UserPO;
import cn.wnhyang.coolGuard.system.vo.user.UserPageVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户信息表
 *
 * @author wnhyang
 * @since 2023/05/14
 */
@Mapper
public interface UserMapper extends BaseMapperX<UserPO> {

    default UserPO selectByUsername(String username) {
        return selectOne(UserPO::getUsername, username);
    }

    default PageResult<UserPO> selectPage(UserPageVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<UserPO>()
                .likeIfPresent(UserPO::getUsername, reqVO.getUsername())
                .likeIfPresent(UserPO::getMobile, reqVO.getMobile())
                .eqIfPresent(UserPO::getStatus, reqVO.getStatus())
                .betweenIfPresent(UserPO::getCreateTime, reqVO.getStartTime(), reqVO.getEndTime())
                .orderByDesc(UserPO::getId));
    }

    default UserPO selectByMobile(String mobile) {
        return selectOne(UserPO::getMobile, mobile);
    }

    default UserPO selectByEmail(String email) {
        return selectOne(UserPO::getEmail, email);
    }

    default List<UserPO> selectListByNickname(String nickname) {
        return selectList(new LambdaQueryWrapperX<UserPO>().like(UserPO::getNickname, nickname));
    }
}
