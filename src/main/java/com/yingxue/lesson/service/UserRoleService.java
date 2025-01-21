package com.yingxue.lesson.service;

import com.yingxue.lesson.vo.req.UserOwnRoleReqVO;

import java.util.List;

public interface UserRoleService {

    List<String> getRoleIdsByUserId(String userId);

    //保存用户角色关联数据后端接口
    void addUserRoleInfo(UserOwnRoleReqVO vo);

    //关联的角色和用户ID接口
    List<String> getUserIdsByRoleIds(List<String> roleIds);


//    List<String> getUserIdsBtRoleId(String roleId);
//    int removeUserRoleId(String roleId);
}
