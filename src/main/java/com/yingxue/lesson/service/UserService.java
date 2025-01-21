package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.vo.req.*;
import com.yingxue.lesson.vo.resp.LoginRespVO;
import com.yingxue.lesson.vo.resp.PageVO;
import com.yingxue.lesson.vo.resp.UserOwnRoleRespVO;

import java.util.List;

public interface UserService {

    //用户登录接口
    LoginRespVO login(LoginReqVO vo);

    //用户退出登录接口
    void logout(String accessToken, String refreshToken);

    PageVO<SysUser> pageInfo(UserPageReqVO vo);

    //新增用户接口
    void addUser(UserAddReqVO vo);

    //根据用户id获取用户角色
    UserOwnRoleRespVO getUserOwnRole(String userId);

    //保存用户拥有的角色接口
    void setUserOwnRole(UserOwnRoleReqVO vo);

    //后端刷新jwt接口
    String refreshToken(String refreshToken);

    //编辑用户后台接口
    void updateUserInfo(UserUpdateReqVO vo, String operationId);

    //批量删除用户
    void deletedUsers(List<String> userIds, String operationId);

    //根据部门id集合查找用户
    List<SysUser> selectUserInfoByDeptIds(List<String> deptIds);

    //获取用户信息接口
    SysUser detailInfo(String userId);

    //个人用户信息编辑接口
    void userUpdateDetailInfo(UserUpdateDetailInfoReqVO vo, String userId);

    void userUpdatePwd(UserUpdatePwdReqVO vo, String accessToken, String refreshToken);


}
