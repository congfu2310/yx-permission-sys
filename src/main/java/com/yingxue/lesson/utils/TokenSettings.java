package com.yingxue.lesson.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;

//读取和存储与 JWT（JSON Web Token）相关的配置项
@Component
@Data
//这个注解用于从配置文件中读取以jwt为前缀的配置项，并将这些配置项映射到TokenSettings类的字段上
@ConfigurationProperties(prefix = "jwt")
public class TokenSettings {
    private String secretKey;
    private Duration accessTokenExpireTime;
    private Duration refreshTokenExpireTime;
    private Duration refreshTokenExpireAppTime;
    private String issuer;
}
