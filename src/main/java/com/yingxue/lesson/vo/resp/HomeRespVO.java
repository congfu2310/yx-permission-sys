package com.yingxue.lesson.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class HomeRespVO {
    @ApiModelProperty(value = "用户信息")
    private UserInfoRespVO userInfoVO;
    @ApiModelProperty(value = "首页菜单导航数据")
    private List<PermissionRespNodeVO> menus;

}
