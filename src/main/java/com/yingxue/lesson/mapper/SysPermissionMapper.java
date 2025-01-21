package com.yingxue.lesson.mapper;

import com.yingxue.lesson.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysPermissionMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysPermission record);

    int insertSelective(SysPermission record);

    SysPermission selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysPermission record);

    int updateByPrimaryKey(SysPermission record);

    List<SysPermission> selectAll();

    //查询关联的子类
    List<SysPermission> selectChild(String pid);

     List<SysPermission> selectInfoByIds (List<String> ids);
}
