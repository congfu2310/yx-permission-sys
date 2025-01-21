package com.yingxue.lesson.mapper;

import com.yingxue.lesson.entity.SysDept;
import com.yingxue.lesson.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysDeptMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysDept record);

    int insertSelective(SysDept record);

    SysDept selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysDept record);

    int updateByPrimaryKey(SysDept record);

    //查询所有的部门数据
    List<SysDept> selectAll();

    //维护新的层级关系
    int updateRelationCode(@Param("oldStr") String oldStr, @Param("newStr") String newStr, @Param("relationCode") String relationCode);


    //通过层级关系查找所有叶子结点dao层接口
    List<String> selectChildIds(String relationCode);


}
