package com.yingxue.lesson.controller;

import com.yingxue.lesson.entity.SysDept;
import com.yingxue.lesson.service.DeptService;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.vo.req.DeptAddReqVO;
import com.yingxue.lesson.vo.req.DeptUpdateReqVO;
import com.yingxue.lesson.vo.resp.DeptRespNodeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/api")
@RestController
@Api(tags = "组织模块-机构管理")
public class DeptController {
    @Autowired
    private DeptService deptService;

    @GetMapping("/depts")
    @ApiOperation(value = "查询所有部门数据的接口")
    @RequiresPermissions("sys:dept:list")
    public DataResult<List<SysDept>> getDeptAll() {
        DataResult<List<SysDept>> result = DataResult.success();
        result.setData(deptService.selectAll());
        return result;
    }


    @GetMapping("/dept/tree")
    @ApiOperation(value = "树型部门列表接口")
    @RequiresPermissions(value = {"sys:user:update","sys:user:add","sys:dept:add","sys:dept:update"},logical = Logical.OR)
    public DataResult<List<DeptRespNodeVO>> getTree() {
        DataResult<List<DeptRespNodeVO>> result = DataResult.success();
        result.setData(deptService.deptTreeList());
        return result;
    }

    @PostMapping("/dept")
    @ApiOperation(value = "新增部门接口")
    @RequiresPermissions("sys:dept:add")
    public DataResult<SysDept> addDept(@RequestBody @Valid DeptAddReqVO vo) {
        DataResult<SysDept> result = DataResult.success();
        result.setData(deptService.addDept(vo));
        return result;
    }

    @PutMapping("/dept")
    @ApiOperation(value = "更新部门信息接口")
    @RequiresPermissions("sys:dept:update")
    public DataResult updateDept(@RequestBody @Valid DeptUpdateReqVO vo) {
        deptService.updateDept(vo);
        return DataResult.success();
    }

    @DeleteMapping("/dept/{id}")
    @ApiOperation(value = "删除部门接口")
    @RequiresPermissions("sys:dept:delete")
    public DataResult deleted(@PathVariable("id") String id) {
        deptService.deleted(id);
        DataResult result = DataResult.success();
        return result;
    }

}
