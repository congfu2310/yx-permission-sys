package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysPermission;
import com.yingxue.lesson.vo.req.PermissionAddReqVO;
import com.yingxue.lesson.vo.req.PermissionUpdateReqVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;
import java.util.List;



public interface PermissionService {
    List<SysPermission> selectAll();

    List<PermissionRespNodeVO> selectAllMenuByTree();

    //新增保存菜单权限
    SysPermission addPermission(PermissionAddReqVO vo);

    //创建根据用户id获取菜单权限接口
    List<PermissionRespNodeVO> permissionTreeList(String userId);

    //查询所有的
    List<PermissionRespNodeVO> selectAllTree();

    //更新菜单权限
    void updatePermission(PermissionUpdateReqVO vo);

    //菜单权限管理-删除操作
    void deletedPermission(String permissionId);


    List<String> getPermissionsByUserId(String userId);


    List<SysPermission> getPermission(String userId);



}
