package com.yingxue.lesson.mapper;

import com.yingxue.lesson.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysUserRole record);

    int insertSelective(SysUserRole record);

    SysUserRole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysUserRole record);

    int updateByPrimaryKey(SysUserRole record);


    //通过用户查询关联的角色id集合
    //用户管理-赋予用户角色
    List<String> getRoleIdsByUserId(String userId);


    //删除用户相关角色
    int removeRoleByUserId(String userId);

    //用户新增角色
    int batchInsertUserRole(List<SysUserRole> list);


    //获取和该角色集合相关连的用户ids
    List<String> getUserIdsByRoleIds(List<String> roleIds);

    //通过角色id 获取跟该角色关联的用户id
    List<String> getUserIdsByRoleId(String roleId);

}
