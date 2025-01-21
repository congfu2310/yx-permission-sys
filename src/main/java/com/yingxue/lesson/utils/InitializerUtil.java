package com.yingxue.lesson.utils;

import org.springframework.stereotype.Component;

//通过构造函数注入，可以确保 JwtTokenUtil 的静态变量在应用程序启动时被正确初始化
@Component
public class InitializerUtil {
    private TokenSettings tokenSettings;
    public InitializerUtil(TokenSettings tokenSettings){
        JwtTokenUtil.setJwtProperties(tokenSettings);
    }
}
