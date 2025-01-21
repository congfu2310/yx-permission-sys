package com.yingxue.lesson.controller;

import com.yingxue.lesson.aop.annotation.MyLog;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.service.UserService;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.utils.JwtTokenUtil;
import com.yingxue.lesson.vo.req.*;
import com.yingxue.lesson.vo.resp.LoginRespVO;
import com.yingxue.lesson.vo.resp.PageVO;
import com.yingxue.lesson.vo.resp.UserOwnRoleRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api")
@Api(tags = "用户模块相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/login")
    @ApiOperation(value = "用户登录接口")
    public DataResult<LoginRespVO> login(@RequestBody @Valid LoginReqVO vo) {
        DataResult result = DataResult.success();
        result.setData(userService.login(vo));
        return result;
    }

    @GetMapping("/user/logout")
    @ApiOperation(value = "用户登出接口")
    public DataResult logout(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
            String refreshToken = request.getHeader(Constant.REFRESH_TOKEN);
            userService.logout(accessToken, refreshToken);
        } catch (Exception e) {
            log.error("logout error{}", e);
        }
        return DataResult.success();
    }


    @GetMapping("/user/unLogin")
    @ApiOperation(value = "引导客户端去登录")
    public DataResult unLogin() {
        DataResult result = DataResult.getResult(BaseResponseCode.TOKEN_ERROR);
        return result;
    }

    @PostMapping("/users")
    @ApiOperation(value = "分页查询用户接口")
    @RequiresPermissions("sys:user:list")
    public DataResult<PageVO<SysUser>> pageInfo(@RequestBody UserPageReqVO vo) {
        DataResult result = DataResult.success();
        result.setData(userService.pageInfo(vo));
        return result;
    }


    @PostMapping("/user")
    @ApiOperation(value = "新增用户接口")
    @RequiresPermissions("sys:user:add")
    public DataResult addUser(@RequestBody @Valid UserAddReqVO vo) {
        userService.addUser(vo);
        return DataResult.success();
    }


    @GetMapping("/user/roles/{userId}")
    @ApiOperation(value = "赋予角色-获取用户拥有角色接口")
    @RequiresPermissions("sys:user:role:update")
    public DataResult<UserOwnRoleRespVO> getUserOwnRole(@PathVariable("userId") String userId) {
        DataResult<UserOwnRoleRespVO> result = DataResult.success();
        result.setData(userService.getUserOwnRole(userId));
        return result;
    }

    //重新签发token刷新token
    @PutMapping("/user/roles")
    @ApiOperation(value = "保存/更新用户拥有的角色信息接口")
    @RequiresPermissions("sys:user:role:update")
    public DataResult saveUserOwnRole(@RequestBody @Valid UserOwnRoleReqVO vo) {
        DataResult result = DataResult.success();
        userService.setUserOwnRole(vo);
        return result;
    }


    @GetMapping("/user/token")
    @ApiOperation(value = "用户刷新token接口")
    public DataResult<String> refreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader(Constant.REFRESH_TOKEN);
        DataResult<String> result = DataResult.success();
        result.setData(userService.refreshToken(refreshToken));
        return result;
    }

    @PutMapping("/user")
    @ApiOperation(value = "列表更新用户信息接口")
    @RequiresPermissions("sys:user:update")
    public DataResult updateUserInfo(@RequestBody @Valid UserUpdateReqVO vo, HttpServletRequest request) {
        String operationId = JwtTokenUtil.getUserId(request.getHeader(Constant.ACCESS_TOKEN));
        userService.updateUserInfo(vo, operationId);
        return DataResult.success();
    }


    //批量删除用户
    @DeleteMapping("/user")
    @ApiOperation(value = "删除用户接口")
    @RequiresPermissions("sys:user:delete")
    public DataResult deletedUser(@RequestBody @ApiParam(value = "用户id集合") List<String> userIds,
                                  HttpServletRequest request) {
        String operationId = JwtTokenUtil.getUserId(request.getHeader(Constant.ACCESS_TOKEN));
        userService.deletedUsers(userIds, operationId);
        return DataResult.success();
    }


    @GetMapping("/user/info")
    @ApiOperation(value = "查询用户详情接口")
    @MyLog(title = "用户管理", action = "查询用户详情")
    public DataResult<SysUser> detailInfo(HttpServletRequest request) {
        String id = JwtTokenUtil.getUserId(request.getHeader(Constant.ACCESS_TOKEN));
        DataResult result = DataResult.success();
        result.setData(userService.detailInfo(id));
        return result;
    }

    @PutMapping("/user/pwd")
    @ApiOperation(value = "更新用户信息接口")

    public DataResult updatePwd(@RequestBody @Valid UserUpdatePwdReqVO vo, HttpServletRequest request) {
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String refreshToken = request.getHeader(Constant.REFRESH_TOKEN);
        DataResult result = DataResult.success();
        userService.userUpdatePwd(vo, accessToken, refreshToken);
        return result;
    }
}
