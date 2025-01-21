package com.yingxue.lesson.service.impl;

import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.entity.SysRole;
import com.yingxue.lesson.entity.SysRolePermission;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysRolePermissionMapper;
import com.yingxue.lesson.mapper.SysUserRoleMapper;
import com.yingxue.lesson.service.RolePermissionService;
import com.yingxue.lesson.vo.req.RolePermissionOperationReqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {
    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public void addRolePermission(RolePermissionOperationReqVO vo) {
        sysRolePermissionMapper.removeByRoleId(vo.getRoleId());
        if (vo.getPermissionIds() == null || vo.getPermissionIds().isEmpty()) {
            return;
        }
        Date createTime = new Date();
        List<SysRolePermission> list = new ArrayList<>();
        //按条插入数据库中
        for (String permissionId : vo.getPermissionIds()) {
            SysRolePermission sysRolePermission = new SysRolePermission();
            sysRolePermission.setId(UUID.randomUUID().toString());
            sysRolePermission.setCreateTime(createTime);
            sysRolePermission.setPermissionId(permissionId);
            sysRolePermission.setRoleId(vo.getRoleId());
            list.add(sysRolePermission);
        }
        int count = sysRolePermissionMapper.batchRolePermission(list);
        if (count == 0) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public List<String> getRoleIdsByPermissionId(String permissionId) {
        return sysRolePermissionMapper.getRoleIdsByPermissionId(permissionId);
    }

    @Override
    public int removeByPermissionId(String permissionId) {
        return sysRolePermissionMapper.removeByPermissionId(permissionId);
    }

    @Override
    public List<String> getPermissionIdsByRoleId(String roleId) {
        return sysRolePermissionMapper.getPermissionIdsByRoleId(roleId);
    }

    @Override
    public int removeByRoleId(String roleId) {
        return sysRolePermissionMapper.removeByRoleId(roleId);
    }

   @Override
    public List<String> getPermissionIdsByRoles(List<String> roleIds) {

        return sysRolePermissionMapper.getPermissionIdsByRoles(roleIds);
    }


}
