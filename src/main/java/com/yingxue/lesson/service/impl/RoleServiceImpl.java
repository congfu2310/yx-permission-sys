package com.yingxue.lesson.service.impl;

import com.github.pagehelper.PageHelper;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.entity.SysRole;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysRoleMapper;
import com.yingxue.lesson.mapper.SysUserRoleMapper;
import com.yingxue.lesson.service.*;
import com.yingxue.lesson.utils.PageUtil;
import com.yingxue.lesson.utils.TokenSettings;
import com.yingxue.lesson.vo.req.AddRoleReqVO;
import com.yingxue.lesson.vo.req.RolePageReqVO;
import com.yingxue.lesson.vo.req.RolePermissionOperationReqVO;
import com.yingxue.lesson.vo.req.RoleUpdateReqVO;
import com.yingxue.lesson.vo.resp.PageVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private RedisService redisService;
    @Autowired
    private TokenSettings tokenSettings;

    @Autowired
    private UserRoleService userRoleService;

    @Override
    //返回类型是 PageVO<SysRole>，表示分页结果
    //方法接收一个 RolePageReqVO 对象 vo 作为参数，RolePageReqVO 包含了分页查询的请求参数
    public PageVO<SysRole> pageInfo(RolePageReqVO vo) {
        PageHelper.offsetPage(vo.getPageNum(), vo.getPageSize());
        //执行数据库查询，获取符合条件的角色列表
        List<SysRole> sysRoles = sysRoleMapper.selectAll(vo);
        return PageUtil.getPageVO(sysRoles);//将查询结果 sysRoles 转换成分页结果对象 PageVO<SysRole> 并返回
    }

    //新增角色的接口
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole addRole(AddRoleReqVO vo) {
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(vo, sysRole);
        sysRole.setId(UUID.randomUUID().toString());
        sysRole.setCreateTime(new Date());
        int i = sysRoleMapper.insertSelective(sysRole);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        //角色和菜单权限表关联操作
        if (vo.getPermissions() != null && !vo.getPermissions().isEmpty()) {
            RolePermissionOperationReqVO operationReqVO = new RolePermissionOperationReqVO();
            operationReqVO.setRoleId(sysRole.getId());
            operationReqVO.setPermissionIds(vo.getPermissions());
            rolePermissionService.addRolePermission(operationReqVO);
        }
        return sysRole;
    }


    //获得所有角色的实现
    @Override
    public List<SysRole> selectAllRoles() {
        return sysRoleMapper.selectAll(new RolePageReqVO());
    }


    //获取角色详情信息
    @Override
    public SysRole detailInfo(String id) {
        //根据id获取角色信息
        SysRole sysRole = sysRoleMapper.selectByPrimaryKey(id);
        if (sysRole == null) {
            log.error("传入的id：{}不合法", id);
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        //获取所有的菜单权限数据封装成树形结构
        List<PermissionRespNodeVO> permissionRespNodes = permissionService.selectAllTree();
        //获取该角色拥有的菜单权限
        List<String> permissionIdsByRoleId = rolePermissionService.getPermissionIdsByRoleId(id);
        Set<String> checkList = new HashSet<>(permissionIdsByRoleId);
        //遍历菜单权限树的数据
        setChecked(permissionRespNodes, checkList);
        sysRole.setPermissionRespNodes(permissionRespNodes);
        return sysRole;
    }


    private void setChecked(List<PermissionRespNodeVO> list, Set<String> checkList) {
        //传入了所有的权限列表和checklist被选中的权限
        for (PermissionRespNodeVO node : list) {
            //检查当前节点是否在选中列表中
            //进一步检查当前节点是否为叶子节点
            if (checkList.contains(node.getId()) &&
                    (node.getChildren() == null || node.getChildren().isEmpty())) {
                //如果满足上述条件，将当前节点的 checked 属性设置为 true
                node.setChecked(true);
            }
            setChecked((List<PermissionRespNodeVO>) node.getChildren(), checkList);
        }
    }


    @Override
    public void updateRole(RoleUpdateReqVO vo) {
        //保存角色基本信息
        SysRole sysRole = sysRoleMapper.selectByPrimaryKey(vo.getId());
        if (null == sysRole) {
            log.error("传入的id：{}不合法", vo.getId());
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        BeanUtils.copyProperties(vo, sysRole);
        sysRole.setUpdateTime(new Date());
        int count = sysRoleMapper.updateByPrimaryKeySelective(sysRole);
        if (count != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //修改了角色关联的菜单权限
        RolePermissionOperationReqVO reqVO = new RolePermissionOperationReqVO();
        reqVO.setRoleId(sysRole.getId());
        reqVO.setPermissionIds(vo.getPermissions());
        rolePermissionService.addRolePermission(reqVO);
        List<String> userIds = sysUserRoleMapper.getUserIdsByRoleId(vo.getId());
        /**
         * 因为用户所拥有的菜单权限是通过角色去关联的
         * 所以要把跟这个角色关联的用户 都要更新 Redis 缓存中的一个键值对
         * */
        if (!userIds.isEmpty()) {
            for (String userId : userIds) {
                redisService.set(Constant.JWT_REFRESH_KEY + userId, userId, tokenSettings.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                //删除用户缓存
                redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deletedRole(String id) {
        SysRole sysRole = new SysRole();
        sysRole.setId(id);
        sysRole.setUpdateTime(new Date());
        sysRole.setDeleted(0);
        int count = sysRoleMapper.updateByPrimaryKeySelective(sysRole);
        if (count != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        List<String> userIds = sysUserRoleMapper.getUserIdsByRoleId(id);
        sysUserRoleMapper.removeRoleByUserId(id);
        rolePermissionService.removeByRoleId(id);
        /**
         * 刪除角色后 要主动去刷新跟該角色有关联用户的token * 因为用户所拥有的菜单权限是通过角色去关联的
         * 所以要把跟这个角色关联的用户 都要重新刷新token
         */
        if (!userIds.isEmpty()) {
            for (String userId : userIds) {
                redisService.set(Constant.JWT_REFRESH_KEY
                                + userId, userId, tokenSettings.getAccessTokenExpireTime().toMillis(),
                        TimeUnit.MILLISECONDS);
                /**
                 * 用户权鉴缓存 key */
                redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
            }
        }
    }

    @Override
    public List<String> getRoleNames(String userId) {
        List<SysRole> sysRoles = getRoleInfoByUserId(userId);
        if (null == sysRoles || sysRoles.isEmpty()) {
            return null;
        }
        List<String> list = new ArrayList<>();
        for (SysRole sysRole : sysRoles) {
            list.add(sysRole.getName());
        }
        return list;
    }

    @Override
    public List<SysRole> getRoleInfoByUserId(String userId) {

        List<String> roleIds = userRoleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return null;
        }
        return sysRoleMapper.getRoleInfoByIds(roleIds);
    }

    //通过userid拿到角色名称s
    @Override
    public List<String> getNamesByUserId(String userId) {
        List<String> roleIds = userRoleService.getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return null;
        }
        return sysRoleMapper.selectNamesByIds(roleIds);
    }
}


