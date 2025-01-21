package com.yingxue.lesson.controller;

import com.yingxue.lesson.entity.SysPermission;
import com.yingxue.lesson.service.PermissionService;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.vo.req.PermissionAddReqVO;
import com.yingxue.lesson.vo.req.PermissionUpdateReqVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Api(tags = "组织模块-菜单权限管理")
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    @GetMapping("/permissions")
    @ApiOperation(value = "获取所有菜单权限数据")
    @RequiresPermissions("sys:permission:list")
    public DataResult<List<SysPermission>> getAllMenusPermission() {
        DataResult<List<SysPermission>> result = DataResult.success();
        result.setData(permissionService.selectAll());
        return result;
    }

    @PutMapping("/permission")
    @ApiOperation(value = "编辑菜单权限接口")
    @RequiresPermissions("sys:permission:update")
    public DataResult updatePermission(@RequestBody @Valid PermissionUpdateReqVO vo) {
        permissionService.updatePermission(vo);
        return DataResult.success();
    }

    //后端菜单权限树接口
    @GetMapping("/permission/tree")
    @ApiOperation(value = "获取所有目录菜单树接口-查到到目录")
    @RequiresPermissions(value = {"sys:permission:update", "sys:permission:add"}, logical = Logical.OR)
    public DataResult<List<PermissionRespNodeVO>> getAllMenusPermissionTree() {
        DataResult<List<PermissionRespNodeVO>> result = DataResult.success();
        result.setData(permissionService.selectAllMenuByTree());
        return result;
    }

    //新增菜单权限后端接口
    @PostMapping("/permission")
    @ApiModelProperty(value = "新增菜单权限后端接口")
    @RequiresPermissions("sys:permission:add")
    public DataResult<SysPermission> addPermission(@RequestBody @Valid PermissionAddReqVO vo) {
        DataResult<SysPermission> result = DataResult.success();
        result.setData(permissionService.addPermission(vo));
        return result;
    }


    @GetMapping("/permission/tree/all")
    @ApiOperation(value = "获取所有目录菜单树接口-查询到按钮")
    @RequiresPermissions(value = {"sys:role:update", "sys:role:add"}, logical = Logical.OR)
    public DataResult<List<PermissionRespNodeVO>> getAllPermissionTree() {
        DataResult<List<PermissionRespNodeVO>> result = DataResult.success();
        result.setData(permissionService.selectAllTree());
        return result;
    }


    @DeleteMapping("/permission/{permissionId}")
    @ApiOperation(value = "删除菜单权限接口")
    @RequiresPermissions("sys:permission:delete")
    public DataResult deleted(@PathVariable("permissionId") String permissionId) {
        permissionService.deletedPermission(permissionId);
        DataResult result = DataResult.success();
        return result;
    }
}
