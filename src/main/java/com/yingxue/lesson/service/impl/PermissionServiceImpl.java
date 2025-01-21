package com.yingxue.lesson.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.entity.SysPermission;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysPermissionMapper;
import com.yingxue.lesson.service.PermissionService;
import com.yingxue.lesson.service.RedisService;
import com.yingxue.lesson.service.RolePermissionService;
import com.yingxue.lesson.service.UserRoleService;
import com.yingxue.lesson.utils.TokenSettings;
import com.yingxue.lesson.vo.req.PermissionAddReqVO;
import com.yingxue.lesson.vo.req.PermissionUpdateReqVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;
    @Autowired
    private RolePermissionService rolePermissionService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private TokenSettings tokenSettings;


    //获取所有的权限菜单列表
    @Override
    public List<SysPermission> selectAll() {
        List<SysPermission> result = sysPermissionMapper.selectAll();
        //!result.isEmpty() --- 对result进行空指针检查
        if (!result.isEmpty()) {
            // 遍历 result 列表中的每个 SysPermission 对象
            for (SysPermission sysPermission : result) {
                // 通过 sysPermission 对象的 pid 字段获取父权限对象
                SysPermission parent = sysPermissionMapper.selectByPrimaryKey(sysPermission.getPid());
                if (parent != null) {
                    // 如果父权限对象存在，设置当前 sysPermission 对象的 pidName 字段为父权限的名称
                    sysPermission.setPidName(parent.getName());
                }
            }
        }
        return result;
    }


    @Override
    public List<PermissionRespNodeVO> selectAllMenuByTree() {
        //先获取全部菜单权限
        List<SysPermission> list = selectAll();
        List<PermissionRespNodeVO> result = new ArrayList<>();
        PermissionRespNodeVO respNode = new PermissionRespNodeVO();
        //set一个【默认顶级菜单】
        respNode.setId("0");
        respNode.setTitle("默认顶级菜单");
        respNode.setSpread(true);
        respNode.setChildren(getTree(list, true));
        result.add(respNode);
        return result;
    }

    //递归获取菜单树
    //修改菜单权限树递归方法，新增一个参数type:true(只查询目录和菜单) false(查询目录、菜单、按钮权限)
    public List<PermissionRespNodeVO> getTree(List<SysPermission> all, boolean type) {
        List<PermissionRespNodeVO> list = new ArrayList<>();
        if (all == null || all.isEmpty()) {
            return list;
        }
        for (SysPermission sysPermission : all) {
            //先遍历出父级菜单目录
            if (sysPermission.getPid().equals("0")) {
                PermissionRespNodeVO permissionRespNode = new PermissionRespNodeVO();
                BeanUtils.copyProperties(sysPermission, permissionRespNode);
                permissionRespNode.setTitle(sysPermission.getName());
                if (type) {
                    permissionRespNode.setChildren(getChildExcBtn(sysPermission.getId(), all));
                } else {
                    permissionRespNode.setChildren(getChildAll(sysPermission.getId(), all));
                }
                list.add(permissionRespNode);
            }
        }
        return list;
    }


    //递归遍历所有
    //此时传入的id是父菜单的id
    private List<PermissionRespNodeVO> getChildAll(String id, List<SysPermission> all) {
        List<PermissionRespNodeVO> list = new ArrayList<>();
        for (SysPermission sysPermission : all) {
            if (sysPermission.getPid().equals(id)) {
                PermissionRespNodeVO permissionRespNode = new PermissionRespNodeVO();
                BeanUtils.copyProperties(sysPermission, permissionRespNode);
                permissionRespNode.setTitle(sysPermission.getName());
                permissionRespNode.setChildren(getChildAll(sysPermission.getId(), all));
                list.add(permissionRespNode);
            }
        }
        return list;
    }


    //只递归获取目录和菜单
    private List<PermissionRespNodeVO> getChildExcBtn(String id, List<SysPermission> all) {

        List<PermissionRespNodeVO> list = new ArrayList<>();
        for (SysPermission sysPermission : all) {
            if (sysPermission.getPid().equals(id) && sysPermission.getType() != 3) {
                PermissionRespNodeVO permissionRespNode = new PermissionRespNodeVO();
                BeanUtils.copyProperties(sysPermission, permissionRespNode);
                permissionRespNode.setTitle(sysPermission.getName());
                permissionRespNode.setChildren(getChildExcBtn(sysPermission.getId(), all));
                list.add(permissionRespNode);
            }
        }
//        System.out.println("Children for id " + id + ": " + list.size());
        return list;
    }

    //创建根据用户id获取菜单权限的具体实现
    @Override
    public List<PermissionRespNodeVO> permissionTreeList(String userId) {
        List<SysPermission> list = getPermission(userId);
        return getTree(list, true);
    }

    @Override
    public List<SysPermission> getPermission(String userId) {
        //根据用户ID通过表查询拿到用户关联的角色的ID
        List<String> roleIds = userRoleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return null;
        }
        //根据角色ID通过表查询拿到权限IDS
        List<String> permissionIds = rolePermissionService.getPermissionIdsByRoles(roleIds);
        if (permissionIds.isEmpty()) {
            return null;
        }
        //根据权限IDS查询表拿去到该用户有权限的权限信息
        List<SysPermission> result = sysPermissionMapper.selectInfoByIds(permissionIds);
        return result;
    }

    //操作后的菜单类型是目录的时候 父级必须为目录
    //操作后的菜单类型是菜单的时候，父类必须为目录类型
    //操作后的菜单类型是按钮的时候 父类必须为菜单类型
    //菜单权限类型Type: 1=目录 2=菜单 3=按钮
    private void verifyForm(SysPermission sysPermission) {
        // 根据sysPermission的pid从数据库中获取父节点权限
        SysPermission parent = sysPermissionMapper.selectByPrimaryKey(sysPermission.getPid());
        // 根据sysPermission的类型进行不同的验证逻辑
        switch (sysPermission.getType()) {
            case 1:  //当新增的是个目录
                if (parent != null) {
                    // 如果父节点存在
                    // 如果父节点的类型不是1，抛出操作菜单权限目录错误异常 -- 所属菜单必须为组织管理/系统管理
                    if (parent.getType() != 1) {
                        throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_CATALOG_ERROR);
                    }
                } else if (!sysPermission.getPid().equals("0")) {
                    // 如果父节点pid不是"0"
                    // 抛出操作菜单权限目录错误异常 -- 所属菜单必须为组织管理/系统管理
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_CATALOG_ERROR);
                }
                break;
            case 2:
                // 当新增的是菜单 -- 所属菜单必须为信息管理 /组织管理 /测试删除 /系统管理
                if (parent == null || parent.getType() != 1) {
                    // 如果父节点不存在或父节点的类型不是目录，抛出操作菜单权限目录错误异常
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_CATALOG_ERROR);
                }
                // 如果新增的菜单的url为空，抛出操作菜单权限URL不能为空异常
                if (StringUtils.isEmpty(sysPermission.getUrl())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_NOT_NULL);
                }
                break;
            case 3:
                // 若新增的是按钮 -- 所属菜单必须为日志管理/接口管理/菜单权限管理/SQL监控/部门管理/用户管理/角色管理
                // 如果所属菜单不存在或所属菜单的类型不是菜单，抛出操作菜单权限按钮错误异常
                if (parent == null || parent.getType() != 2) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_BTN_ERROR);
                }
                // 如果新增按钮的权限标识(perms)为空，抛出操作菜单权限标识不能为空异常
                if (StringUtils.isEmpty(sysPermission.getPerms())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_PERMS_NULL);
                }
                // 如果新增按钮的url为空，抛出操作菜单权限URL不能为空异常
                if (StringUtils.isEmpty(sysPermission.getUrl())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_NOT_NULL);
                }
                // 如果新增按钮的HTTP方法(method)为空，抛出操作菜单权限HTTP方法不能为空异常
                if (StringUtils.isEmpty(sysPermission.getMethod())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_METHOD_NULL);
                }
                break;
        }
    }

    //接受一个 PermissionAddReqVO 对象作为参数，将其转换为 SysPermission 对象，并执行一些验证和数据库插入操作
    @Override
    public SysPermission addPermission(PermissionAddReqVO vo) {
        // 创建一个新的SysPermission对象
        SysPermission sysPermission = new SysPermission();
        // 将vo对象的属性复制到sysPermission对象中
        BeanUtils.copyProperties(vo, sysPermission);
        // 调用verifyForm方法验证sysPermission对象的有效性
        verifyForm(sysPermission);
        // 为sysPermission中添加设置一个随机生成的UUID作为ID
        sysPermission.setId(UUID.randomUUID().toString());
        // 设置sysPermission的创建时间为当前时间
        sysPermission.setCreateTime(new Date());
        // 插入sysPermission到数据库中，返回受影响的行数
        int count = sysPermissionMapper.insertSelective(sysPermission);
        // 如果插入失败（受影响的行数不为1），抛出操作错误异常
        if (count != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        // 返回插入成功的sysPermission对象
        return sysPermission;
    }

    //获取所有的菜单权限树
    @Override
    public List<PermissionRespNodeVO> selectAllTree() {
        List<SysPermission> list = selectAll();
        return getTree(list, false);
    }


    //菜单权限管理-编辑菜单权限（所属菜单）
    @Override
    public void updatePermission(PermissionUpdateReqVO vo) {
        //属性复制
        SysPermission update = new SysPermission();
        log.info("获取到属性");
        BeanUtils.copyProperties(vo, update);
        //验证数据--具体实现看上面
        verifyForm(update);
        //根据id查询数据库此权限
        SysPermission sysPermission = sysPermissionMapper.selectByPrimaryKey(vo.getId());
        if (sysPermission == null) {
            //这个id不存在我们的数据库中，就打印一个日志，抛出业务异常
            log.info("传入的id不存在{}", vo.getId());
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        if (!sysPermission.getPid().equals(vo.getPid())) {
            //所属菜单发生了变化，要校验该权限是否存在子集
            List<SysPermission> sysPermissions = sysPermissionMapper.selectChild(vo.getId());
            //如果该菜单权限关联了子集叶子节点的话我们就不让操作
            if (!sysPermissions.isEmpty()) {
                throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_UPDATE);
            }
        }
        //保存权限数据-更新数据
        update.setUpdateTime(new Date());
        int i = sysPermissionMapper.updateByPrimaryKeySelective(update);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //如果权限标识符发生了改变,编辑成功后还有标记涉及到的用户,因为菜单权限变更了，要重新签发关联用户的token
        //检查当前权限对象 sysPermission 的权限标识符 perms 是否与更新请求对象 vo 的权限标识符不同
        if (!sysPermission.getPerms().equals(vo.getPerms())) {
            //获取与该权限 ID 相关联的角色 ID
            List<String> roleIdsByPermissionId = rolePermissionService.getRoleIdsByPermissionId(vo.getId());
            if (!roleIdsByPermissionId.isEmpty()) {
                //获取到的角色 ID 列表是否为空。如果不为空，表示有角色与该权限关联，找出这些用户id
                List<String> userIdsByRoleIds = userRoleService.getUserIdsByRoleIds(roleIdsByPermissionId);
                if (!userIdsByRoleIds.isEmpty()) {
                    //遍历与这些角色关联的每一个用户
                    for (String userId :
                            userIdsByRoleIds) {
                        //更新 Redis 缓存,设置新的 JWT 刷新键
                        redisService.set(Constant.JWT_REFRESH_KEY + userId, userId, tokenSettings.getAccessTokenExpireTime(
                        ).toMillis(), TimeUnit.MILLISECONDS);
                        //删除旧的用户权限鉴别缓存键
                        redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
                    }
                }
            }
        }
    }

    @Override
    public void deletedPermission(String permissionId) {
        //判断是否存在子集关联
        List<SysPermission> sysPermissions = sysPermissionMapper.selectChild(permissionId);
        if (!sysPermissions.isEmpty()) {
            throw new BusinessException(BaseResponseCode.ROLE_PERMISSION_RELATION);
        }
        //解除相关角色和该菜单权限的关联
        rolePermissionService.removeByPermissionId(permissionId);
        //更新菜单权限数据，把1--0
        SysPermission sysPermission = new SysPermission();
        sysPermission.setUpdateTime(new Date());
        sysPermission.setDeleted(0);
        sysPermission.setId(permissionId);
        int i = sysPermissionMapper.updateByPrimaryKeySelective(sysPermission);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //需要标注和该菜单权限关联用户(需要主动刷新token 重新签发)
        List<String> roleIdsByPermissionId = rolePermissionService.getRoleIdsByPermissionId(permissionId);
        if (!roleIdsByPermissionId.isEmpty()) {
            List<String> userIdsByRoleIds = userRoleService.getUserIdsByRoleIds(roleIdsByPermissionId);
            if (!userIdsByRoleIds.isEmpty()) {
                for (String userId : userIdsByRoleIds) {
                    redisService.set(Constant.JWT_REFRESH_KEY
                            + userId, userId, tokenSettings.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                    /**
                     * 用户权鉴缓存 key */
                    redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
                }
            }
        }

    }


    @Override
    public List<String> getPermissionsByUserId(String userId) {
        List<SysPermission> permissions = getPermission(userId);
        if (null == permissions || permissions.isEmpty()) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (SysPermission sysPermission : permissions) {
            if (!StringUtils.isEmpty(sysPermission.getPerms())) {
                result.add(sysPermission.getPerms());
            }

        }
        return result;
    }


}

