package com.yingxue.lesson.service.impl;

import com.github.pagehelper.PageHelper;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.entity.SysDept;
import com.yingxue.lesson.entity.SysRole;
import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysDeptMapper;
import com.yingxue.lesson.mapper.SysUserMapper;
import com.yingxue.lesson.service.*;
import com.yingxue.lesson.utils.*;
import com.yingxue.lesson.vo.req.*;
import com.yingxue.lesson.vo.resp.LoginRespVO;
import com.yingxue.lesson.vo.resp.PageVO;
import com.yingxue.lesson.vo.resp.UserOwnRoleRespVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TokenSettings tokenSettings;

    @Autowired
    private PermissionService permissionService;

    //登录的具体实现方法
    @Override
    public LoginRespVO login(LoginReqVO vo) {
        //通过用户名查询用户信息
        SysUser userInfoByName = sysUserMapper.getUserInfoByName(vo.getUsername());
        //检查用户是否存在，不存在抛出异常
        if (userInfoByName == null) {
            throw new BusinessException(BaseResponseCode.ACCOUNT_ERROR);
        }
        //检查用户状态，账户状态(1.正常 2.锁定 )
        if (userInfoByName.getStatus() == 2) {
            throw new BusinessException(BaseResponseCode.ACCOUNT_LOCK_TIP);
        }
        //验证密码-检查用户提供的密码是否与存储在数据库中的密码匹配
        if (!PasswordUtils.matches(userInfoByName.getSalt(), vo.getPassword(), userInfoByName.getPassword())) {
            throw new BusinessException(BaseResponseCode.ACCOUNT_PASSWORD_ERROR);
        }

        //创建一个 LoginRespVO 对象，并填充用户的电话号码、用户名和 ID
        LoginRespVO loginRespVO = new LoginRespVO();
        loginRespVO.setPhone(userInfoByName.getPhone());
        loginRespVO.setUsername(userInfoByName.getUsername());
        loginRespVO.setId(userInfoByName.getId());

        //创建 JWT 令牌的 Claims
        //创建一个 claims 映射，包含用户的角色、权限和用户名
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constant.JWT_ROLES_KEY, getRolesByUserId(userInfoByName.getId()));
        claims.put(Constant.JWT_PERMISSIONS_KEY, getPermissionsByUserId(userInfoByName.getId()));
        claims.put(Constant.JWT_USER_NAME, userInfoByName.getUsername());
        //生成访问令牌 accessToken
        String accessToken = JwtTokenUtil.getAccessToken(userInfoByName.getId(), claims);
        //根据请求的类型（vo.getType()）---"登录类型 1：pc；2：App"---生成不同的刷新令牌 --- refreshToken
        String refreshToken;
        if (vo.getType().equals("1")) {
            refreshToken = JwtTokenUtil.getRefreshToken(userInfoByName.getId(), claims);
        } else {
            refreshToken = JwtTokenUtil.getRefreshAppToken(userInfoByName.getId(), claims);
        }
        //将生成的访问令牌和刷新令牌设置到 LoginRespVO 对象中,并返回
        loginRespVO.setAccessToken(accessToken);
        loginRespVO.setRefreshToken(refreshToken);
        return loginRespVO;
    }

    //通过用户id获取该用户所拥有的角色
    private List<String> getRolesByUserId(String userId) {
        return roleService.getRoleNames(userId);
    }

    //通过用户id获取该用户所拥有的权限
    private List<String> getPermissionsByUserId(String userId) {
        return permissionService.getPermissionsByUserId(userId);
    }


    @Override
    public void logout(String accessToken, String refreshToken) {
        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken)) {
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        //获取当前用户的 Subject 对象
        //Subject 是 Shiro 的核心接口，代表当前用户的会话
        Subject subject = SecurityUtils.getSubject();
        log.info("subject.getPrincipals()={}", subject.getPrincipals());
        //如果用户已经认证（登录状态），则执行登出操作
        if (subject.isAuthenticated()) {
            subject.logout();
        }
        String userId = JwtTokenUtil.getUserId(accessToken);
        /**
         * 把token 加入黑名单 禁止再登录
         */
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST + accessToken, userId, JwtTokenUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);
        /**
         * 把 refreshToken 加入黑名单 禁止再拿来刷新token
         */
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST + refreshToken, userId, JwtTokenUtil.getRemainingTime(refreshToken), TimeUnit.MILLISECONDS);
    }

    //分页查询的实现
    @Override
    public PageVO<SysUser> pageInfo(UserPageReqVO vo) {
        //启动分页查询，设置当前页码和每页显示的记录数
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        List<SysUser> list = sysUserMapper.selectAll(vo);
        //修改分页接口加上所属部门字段
        for (SysUser sysUser : list) {
            SysDept sysDept = sysDeptMapper.selectByPrimaryKey(sysUser.getDeptId());
            if (sysDept != null) {
                sysUser.setDeptName(sysDept.getName());
            }
        }
        return PageUtil.getPageVO(list);
    }


    //新增用户的实现
    @Override
    public void addUser(UserAddReqVO vo) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setSalt(PasswordUtils.getSalt());
        String encode = PasswordUtils.encode(vo.getPassword(), sysUser.getSalt());
        sysUser.setPassword(encode);
        sysUser.setId(UUID.randomUUID().toString());
        sysUser.setCreateTime(new Date());
        int i = sysUserMapper.insertSelective(sysUser);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }


    //根据用户id获取用户角色
    @Override
    public UserOwnRoleRespVO getUserOwnRole(String userId) {
        List<String> roleIdsByUserId = userRoleService.getRoleIdsByUserId(userId);
        List<SysRole> list = roleService.selectAllRoles();
        UserOwnRoleRespVO vo = new UserOwnRoleRespVO();
        vo.setAllRole(list);
        vo.setOwnRoles(roleIdsByUserId);
        return vo;
    }

    //保存用户拥有的角色实现
    @Override
    public void setUserOwnRole(UserOwnRoleReqVO vo) {
        userRoleService.addUserRoleInfo(vo);
        //标记用户 要主动去刷新token
        //重新签发token
        redisService.set(Constant.JWT_REFRESH_KEY + vo.getUserId(), vo.getUserId(), tokenSettings.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
        redisService.delete(Constant.IDENTIFY_CACHE_KEY + vo.getUserId());
    }


    //jwt 自动刷新 jwt 刷新有两种情况要考虑?
    //一种是管理员修改了该用户的角色/权限(需要主动去刷新)。
    //另一种是 jwt 过期要刷新：刷新成功后生成新的token(如果是主动刷新的话就要标记新生成的token)自动再刷新当前请求接口
    @Override
    public String refreshToken(String refreshToken) {
        //校验这个刷新token是否有效
        //校验刷新token是都加入黑名单
        //用户主动退出或者refreshToken已经过期
        if (redisService.hasKey(Constant.JWT_ACCESS_TOKEN_BLACKLIST + refreshToken) || !JwtTokenUtil.validateToken(refreshToken)) {
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }
        String userId = JwtTokenUtil.getUserId(refreshToken);
        log.info("userId={}", userId);
        Map<String, Object> claims = null;
        // 用户主动去刷新
        //更新角色/权限信息
        if (redisService.hasKey(Constant.JWT_REFRESH_KEY + userId)) {
            claims = new HashMap<>();
            claims.put(Constant.JWT_ROLES_KEY, getRolesByUserId(userId));
            claims.put(Constant.JWT_PERMISSIONS_KEY, getPermissionsByUserId(userId));
        }
        String newAccessToken = JwtTokenUtil.refreshToken(refreshToken, claims);
        // 如果是主动去刷新着 redis 标记新的access_token
        //过期时间为 key=Constant.JWT_REFRESH_KEY+userId 的剩余过期时间
        if (redisService.hasKey(Constant.JWT_REFRESH_KEY + userId)) {
            redisService.set(Constant.JWT_REFRESH_IDENTIFICATION + newAccessToken, userId, redisService.getExpire(Constant.JWT_REFRESH_KEY + userId, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
        return newAccessToken;
    }

    //编辑用户后台实现
    //operationId是操作人的userId
    @Override
    public void updateUserInfo(UserUpdateReqVO vo, String operationId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(operationId);
        if (!StringUtils.isEmpty(vo.getPassword())) {
            String newPassword = PasswordUtils.encode(vo.getPassword(), sysUser.getSalt());
            sysUser.setPassword(newPassword);
        } else {
            sysUser.setPassword(null);
        }
        int count = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if (count != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //说明用户状态有改变 加入变成禁用，之前,签发的token 都要失效
        //status状态为2是账号禁用
        if (vo.getStatus() == 2) {
            redisService.set(Constant.ACCOUNT_LOCK_KEY + sysUser.getId(), sysUser.getId());
        } else {
            redisService.delete(Constant.ACCOUNT_LOCK_KEY + sysUser.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletedUsers(List<String> userIds, String operationId) {
        SysUser sysUser = new SysUser();
        sysUser.setUpdateId(operationId);
        sysUser.setUpdateTime(new Date());
        sysUser.setDeleted(0);
        int i = sysUserMapper.deletedUsers(sysUser, userIds);
        if (i == 0) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //标记用户id 已删除
        //因为我们是以签发token 的形式保持用户登录状态的
        //有可能签发了多次token 所以在用户删除的时候
        //要把userId标记起来 过期时间为refreshToken 的过期时间 * 避免它可以通过刷新token来继续保持登录
        for (String userId : userIds) {
            redisService.set(Constant.DELETED_USER_KEY + userId, userId, tokenSettings.getRefreshTokenExpireAppTime().toMillis(), TimeUnit.MILLISECONDS);
            /**
             * 用户权鉴缓存 key */
            redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
        }
    }

    @Override
    public List<SysUser> selectUserInfoByDeptIds(List<String> deptIds) {
        return sysUserMapper.selectUserInfoByDeptIds(deptIds);
    }

    @Override
    public SysUser detailInfo(String userId) {
        return sysUserMapper.selectByPrimaryKey(userId);
    }

    @Override
    public void userUpdateDetailInfo(UserUpdateDetailInfoReqVO vo, String userId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setId(userId);
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(userId);
        int count = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if (count != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public void userUpdatePwd(UserUpdatePwdReqVO vo, String accessToken, String refreshToken) {
        String userId = JwtTokenUtil.getUserId(accessToken);
        //校验旧密码
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        if (sysUser == null) {
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        if (!PasswordUtils.matches(sysUser.getSalt(), vo.getOldPwd(), sysUser.getPassword())) {
            throw new BusinessException(BaseResponseCode.OLD_PASSWORD_ERROR);
        }
        //把新的密码保存进数据库
        sysUser.setUpdateTime(new Date());
        sysUser.setPassword(PasswordUtils.encode(vo.getNewPwd(), sysUser.getSalt()));
        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        // 把token 加入黑名单 禁止再登录
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST + accessToken, userId, JwtTokenUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);
        // 把 refreshToken 加入黑名单 禁止再拿来刷新token
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST + refreshToken, userId, JwtTokenUtil.getRemainingTime(refreshToken), TimeUnit.MILLISECONDS);
    }
}




