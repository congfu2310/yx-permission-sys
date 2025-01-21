package com.yingxue.lesson.service;

import com.yingxue.lesson.entity.SysDept;
import com.yingxue.lesson.vo.req.DeptAddReqVO;
import com.yingxue.lesson.vo.req.DeptUpdateReqVO;
import com.yingxue.lesson.vo.resp.DeptRespNodeVO;

import java.util.List;

public interface DeptService {
    List<SysDept> selectAll();

    //部门树接口
    List<DeptRespNodeVO> deptTreeList();

    //新增部门接口
    SysDept addDept(DeptAddReqVO vo);

    //部门-编辑功能接口
    void updateDept(DeptUpdateReqVO vo);


    void deleted(String id);
}
