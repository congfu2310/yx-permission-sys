package com.yingxue.lesson.utils;

import com.yingxue.lesson.constants.Constant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtTokenUtil {

    //静态变量
    private static String secretKey;////密钥
    private static Duration accessTokenExpireTime; //访问令牌的过期时间--令牌在多长时间后需要重新获取或刷新。
    private static Duration refreshTokenExpireTime;  //刷新令牌的过期时间
    private static Duration refreshTokenExpireAppTime; // 应用程序级别的刷新令牌的过期时间
    private static String issuer;//签发人

    //将 TokenSettings 中的配置值设置到静态变量中，以便在 JwtTokenUtil 类中使用
    public static void setJwtProperties(TokenSettings tokenSettings) {
        secretKey = tokenSettings.getSecretKey();
        accessTokenExpireTime = tokenSettings.getAccessTokenExpireTime();
        refreshTokenExpireTime = tokenSettings.getRefreshTokenExpireTime();
        refreshTokenExpireAppTime = tokenSettings.getRefreshTokenExpireAppTime();
        issuer = tokenSettings.getIssuer();
    }


    //签发token
    // issuer    签发人
    //subject   代表这个JWT的主体，即它的所有人 一般是用户id
    //claims    存储在JWT里面的信息 一般放些用户的权限/角色信息
    // ttlMillis 有效时间(毫秒)
    public static String generateToken(String issuer, String subject, Map<String, Object> claims, long ttlMillis, String secret) {
        //定义用于签名 token 的算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        //获取当前时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        byte[] signingKey = DatatypeConverter.parseBase64Binary(secret);

        JwtBuilder builder = Jwts.builder();
        if (null != claims) {
            builder.setClaims(claims);
        }
        if (!StringUtils.isEmpty(subject)) {
            builder.setSubject(subject);
        }
        if (!StringUtils.isEmpty(issuer)) {
            builder.setIssuer(issuer);
        }
        builder.setIssuedAt(now);
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        builder.signWith(signatureAlgorithm, signingKey);
        //返回生成的 token
        return builder.compact();
    }

    //生成 access_token
    public static String getAccessToken(String subject, Map<String, Object> claims) {

        return generateToken(issuer, subject, claims, accessTokenExpireTime.toMillis(), secretKey);
    }


    // 上面我们已经有生成 access_token 的方法，下面加入生成 refresh_token 的方法(PC 端过期时间短一些)
    public static String getRefreshToken(String subject, Map<String, Object> claims) {
        return generateToken(issuer, subject, claims, refreshTokenExpireTime.toMillis(), secretKey);
    }

    //生产 App端 refresh_token
    public static String getRefreshAppToken(String subject, Map<String, Object> claims) {
        return generateToken(issuer, subject, claims, refreshTokenExpireAppTime.toMillis(), secretKey);
    }


    //从令牌中解析，获取数据
    public static Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(secretKey)).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    //获取用户id
    public static String getUserId(String token) {
        String userId = null;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            log.error("eror={}", e);
        }
        return userId;
    }

    //获取用户名
    public static String getUserName(String token) {

        String username = null;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims.get(Constant.JWT_USER_NAME);
        } catch (Exception e) {
            log.error("eror={}", e);
        }
        return username;
    }

    //校验令牌
    public static Boolean validateToken(String token) {
        Claims claimsFromToken = getClaimsFromToken(token);
        //检查提取到的声明是否为 null，或检查令牌是否已过期
        return (null != claimsFromToken && !isTokenExpired(token));
    }

    //验证token 是否过期
    public static Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("error={}", e);
            return true;
        }
    }

    // 刷新token
    public static String refreshToken(String refreshToken, Map<String, Object> claims) {
        String refreshedToken;
        try {
            Claims parserclaims = getClaimsFromToken(refreshToken);
            /**
             * 刷新token的时候如果为空说明原先的 用户信息不变 所以就引用上个token里的内容
             */
            if (null == claims) {
                claims = parserclaims;
            }
            refreshedToken = generateToken(parserclaims.getIssuer(), parserclaims.getSubject(), claims, accessTokenExpireTime.toMillis(), secretKey);
        } catch (Exception e) {
            refreshedToken = null;
            log.error("error={}", e);
        }
        return refreshedToken;
    }


    // 获取token的剩余过期时间
    public static long getRemainingTime(String token) {
        long result = 0;
        try {
            long nowMillis = System.currentTimeMillis();
            result = getClaimsFromToken(token).getExpiration().getTime() - nowMillis;
        } catch (Exception e) {
            log.error("error={}", e);
        }
        return result;
    }
}


