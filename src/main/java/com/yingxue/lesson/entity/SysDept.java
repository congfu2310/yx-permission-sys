package com.yingxue.lesson.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysDept implements Serializable {
    //父级名称
    private String pidName;
    private String id;

    private String deptNo;

    private String name;

    private String pid;

    private Integer status;

    private String relationCode;

    private String deptManagerId;

    private String managerName;

    private String phone;

    private Date createTime;

    private Date updateTime;

    private Integer deleted;


}
