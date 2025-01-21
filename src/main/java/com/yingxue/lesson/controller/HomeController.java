package com.yingxue.lesson.controller;

import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.service.HomeService;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.utils.JwtTokenUtil;
import com.yingxue.lesson.vo.resp.HomeRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Api(tags = "首页模块", description = "首页相关模块")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/home")
    @ApiOperation(value = "获取首页数据")
    public DataResult<HomeRespVO> getHomeInfo(HttpServletRequest request) {
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        //通过access_token拿userId
        String userId = JwtTokenUtil.getUserId(accessToken);
        DataResult<HomeRespVO> result = DataResult.success();
        result.setData(homeService.getHomeInfo(userId));
        return result;
    }
}
