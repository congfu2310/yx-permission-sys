package com.yingxue.lesson.mapper;

import com.yingxue.lesson.entity.SysRole;
import com.yingxue.lesson.vo.req.RolePageReqVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysRoleMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysRole record);

    int insertSelective(SysRole record);

    SysRole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysRole record);

    int updateByPrimaryKey(SysRole record);

    //获取所有角色的接口
    List<SysRole> selectAll(RolePageReqVO vo);

    List<SysRole> getRoleInfoByIds(List<String> ids);

    List<String> selectNamesByIds(List<String> roleIds);

}
