package com.yingxue.lesson.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class PermissionRespNodeVO {
    @ApiModelProperty (value = "id")
    private String id;

    @ApiModelProperty (value = "菜单权限名称")
    private String title;

    @ApiModelProperty(value = "接口地址")
    private String url;
    private List<?> children;

    @ApiModelProperty (value = "是否展开，默认不展开（false）")
    private boolean spread = true;

    @ApiModelProperty(value = "节点是否选中")
    private boolean checked;
}
