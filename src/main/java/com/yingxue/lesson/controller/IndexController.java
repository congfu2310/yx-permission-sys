package com.yingxue.lesson.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//跳转视图跳转控制器
//@Controller以便返回视图（HTML页面),方法返回的是视图名，Spring MVC会根据视图解析器将视图名解析为实际的视图页面
@Api(tags = "视图", description = "负责返回视图")
@Controller
@RequestMapping("/index")
public class IndexController {
    @GetMapping("/404")
    @ApiOperation(value = "跳转404错误页面")
    public String error404() {
        return "error/404";
    }

    @GetMapping("/login")
    @ApiOperation(value = "跳转，登录")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    @ApiOperation(value = "跳转首页页面")
    public String home() {
        return "home";
    }

    @GetMapping("/main")
    @ApiOperation(value = "跳转主页页面")
    public String main() {
        return "main";
    }

    @GetMapping("/menus")
    @ApiOperation(value = "跳转菜单权限页面")
    public String menusList() {
        return "menus/menu";
    }

    @GetMapping("/roles")
    @ApiOperation("跳转角色管理页面")
    public String rolesList() {
        return "roles/role";
    }

    @GetMapping("/depts")
    @ApiOperation(value = "跳转部门管理页面")
    public String deptList() {
        return "depts/dept";
    }

    @GetMapping("/users")
    @ApiOperation(value = "跳转用户管理页面")
    public String users() {
        return "users/user";
    }

    @GetMapping("/logs")
    @ApiOperation(value = "跳转日志管理页面")
    public String logList() {
        return "/logs/log";
    }

    @GetMapping("/users/info")
    @ApiOperation(value = "跳转个人用户信息编辑页面")
    public String userDetail() {
        return "/users/user_edit";
    }

    @GetMapping("/users/pwd")
    @ApiOperation(value = "跳转个人用户编辑密码页面")
    public String userPwd() {
        return "/users/user_pwd";
    }
}
