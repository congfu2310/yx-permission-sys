package com.yingxue.lesson.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DeptAddReqVO {
    @ApiModelProperty(value = "部门名称")
    private String name;
    @ApiModelProperty(value = "父级id 一级为 0")
    private String pid;

    @ApiModelProperty(value = "部门经理名称")
    private String managerName;
    @ApiModelProperty(value = "部门经理电话")
    private String phone;
    @ApiModelProperty(value = "机构状态(1:正常;0:弃用)")
    private Integer status;
}
