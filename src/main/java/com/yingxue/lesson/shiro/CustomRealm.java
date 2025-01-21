package com.yingxue.lesson.shiro;

import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.service.PermissionService;
import com.yingxue.lesson.service.RedisService;
import com.yingxue.lesson.service.RoleService;
import com.yingxue.lesson.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

//自定义用户身份验证和授权逻辑
public class CustomRealm extends AuthorizingRealm {
    @Autowired
    private RoleService roleService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RedisService redisService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof CustomUsernamePasswordToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String accessToken = (String) principalCollection.getPrimaryPrincipal();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        String userId = JwtTokenUtil.getUserId(accessToken);
        //比较 Redis 中 JWT_REFRESH_KEY 的剩余过期时间和 accessToken 的剩余时间，
        // 如果 Redis 中的时间更长，则认为 accessToken 是在 JWT_REFRESH_KEY 之后生成的，表示 accessToken 有效
        if (redisService.hasKey(Constant.JWT_REFRESH_KEY + userId) && redisService.getExpire(Constant.JWT_REFRESH_KEY + userId, TimeUnit.MILLISECONDS) > JwtTokenUtil.getRemainingTime(accessToken)) {
            List<String> roleNames = roleService.getRoleNames(userId);
            if (roleNames != null && !roleNames.isEmpty()) {
                info.addRoles(roleNames);
            }
            //从数据库获取角色和权限信息
            List<String> permissions = permissionService.getPermissionsByUserId(userId);
            if (permissions != null) {
                info.addStringPermissions(permissions);
            }
        } else {
            //如果 Redis 中的 JWT_REFRESH_KEY 不存在或其剩余过期时间小于 accessToken 的剩余时间
            // 则从 accessToken 中提取角色和权限信息
            //从 accessToken 中提取 Claims 对象，获取其中的角色和权限信息，并添加到 info 对象中
            Claims claims = JwtTokenUtil.getClaimsFromToken(accessToken);
            if (claims.get(Constant.JWT_ROLES_KEY) != null) {
                info.addRoles((Collection<String>) claims.get(Constant.JWT_ROLES_KEY));
            }
            if (claims.get(Constant.JWT_PERMISSIONS_KEY) != null) {
                info.addStringPermissions((Collection<String>)
                        claims.get(Constant.JWT_PERMISSIONS_KEY));
            }
        }
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws
            AuthenticationException {
        CustomUsernamePasswordToken customUsernamePasswordToken = (CustomUsernamePasswordToken) authenticationToken;
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(customUsernamePasswordToken.getPrincipal(), customUsernamePasswordToken.getCredentials(), CustomRealm.class.getName());
        return info;
    }
}
