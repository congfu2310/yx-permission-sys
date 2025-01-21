package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysRole;
import com.yingxue.lesson.vo.req.AddRoleReqVO;
import com.yingxue.lesson.vo.req.RolePageReqVO;
import com.yingxue.lesson.vo.req.RoleUpdateReqVO;
import com.yingxue.lesson.vo.resp.PageVO;

import java.util.List;


public interface RoleService {

    //获取角色分页接口
    PageVO<SysRole> pageInfo(RolePageReqVO vo);

    //新增角色的接口
    SysRole addRole(AddRoleReqVO vo);

    //获取所有角色接口
    List<SysRole> selectAllRoles();

    //获取角色详情信息
    SysRole detailInfo(String id);

    void updateRole(RoleUpdateReqVO vo);

    void deletedRole(String roleId);

    List<String> getRoleNames(String userId);

    List<SysRole> getRoleInfoByUserId(String userId);

    List<String> getNamesByUserId(String userId);
}
