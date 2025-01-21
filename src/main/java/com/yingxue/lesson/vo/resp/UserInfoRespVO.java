package com.yingxue.lesson.vo.resp;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserInfoRespVO {
    @ApiModelProperty(value ="用户id")
    private String id;
    @ApiModelProperty(value = "账号")
    private String username;
    @ApiModelProperty(value = "所属机构id")
    private String deptId;
    @ApiModelProperty(value = "所属机构名称")
    private String deptName;
}

