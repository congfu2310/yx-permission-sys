package com.yingxue.lesson.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysPermission implements Serializable {
    private String id;

    private String code;

    private String name;

    private String perms;

    private String url;

    private String method;

    private String pid;

    private Integer orderNum;

    private Integer type;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    private Integer deleted;

    //父级对象的name
    private String pidName;
}
