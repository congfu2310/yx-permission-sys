package com.yingxue.lesson.service.impl;

import com.yingxue.lesson.entity.SysUserRole;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysUserRoleMapper;
import com.yingxue.lesson.service.UserRoleService;
import com.yingxue.lesson.vo.req.UserOwnRoleReqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;


    @Override
    public List<String> getRoleIdsByUserId(String userId) {
        return sysUserRoleMapper.getRoleIdsByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addUserRoleInfo(UserOwnRoleReqVO vo) {
        //删除关联数据
        sysUserRoleMapper.removeRoleByUserId(vo.getUserId());
        if (vo.getRoleIds() == null || vo.getRoleIds().isEmpty()) {
            return;
        }
        Date createTime = new Date();
        List<SysUserRole> list = new ArrayList<>();
        //遍历ID集合
        for (
                String roleId : vo.getRoleIds()) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setId(UUID.randomUUID().toString());
            sysUserRole.setCreateTime(createTime);
            sysUserRole.setUserId(vo.getUserId());
            sysUserRole.setRoleId(roleId);
            list.add(sysUserRole);
        }
        //批量插入
        int count = sysUserRoleMapper.batchInsertUserRole(list);
        if (count == 0) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

     @Override
    public List<String> getUserIdsByRoleIds(List<String> roleIds) {
        return sysUserRoleMapper.getUserIdsByRoleIds(roleIds);
    }
}

