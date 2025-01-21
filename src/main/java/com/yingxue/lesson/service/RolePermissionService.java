package com.yingxue.lesson.service;


import com.yingxue.lesson.vo.req.RolePermissionOperationReqVO;

import java.util.List;


public interface RolePermissionService {

    void addRolePermission(RolePermissionOperationReqVO vo);

    List<String> getRoleIdsByPermissionId(String permissionId);

    int removeByPermissionId(String permissionId);

    List<String> getPermissionIdsByRoleId(String roleId);

    //根据角色id删除所有和该角色关联的菜单权限的数据
    int removeByRoleId(String roleId);

    List<String> getPermissionIdsByRoles(List<String> roleIds);

}
