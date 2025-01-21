package com.yingxue.lesson.vo.req;


import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

import javax.validation.constraints.NotBlank;

import java.util.List;

//新增角色的请求vo
@Data
public class AddRoleReqVO {
    @ApiModelProperty(value = "角色id")
    @NotBlank(message = "角色id不能为空")
    private String name;

    @ApiModelProperty(value = "角色描述")
    private String description;
    @ApiModelProperty(value = "状态（1：正常；0：弃用）")
    private Integer status;
    @ApiModelProperty(value = "拥有的权限ID集合")
    private List<String> permissions;
}

