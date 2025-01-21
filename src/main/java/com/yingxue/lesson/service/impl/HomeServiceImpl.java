package com.yingxue.lesson.service.impl;

import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.mapper.SysUserMapper;
import com.yingxue.lesson.service.HomeService;
import com.yingxue.lesson.service.PermissionService;
import com.yingxue.lesson.vo.resp.HomeRespVO;
import com.yingxue.lesson.vo.resp.PermissionRespNodeVO;
import com.yingxue.lesson.vo.resp.UserInfoRespVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private PermissionService permissionService;

    //首页获取导航菜单数据
    @Override
    public HomeRespVO getHomeInfo(String userId) {
        HomeRespVO homeRespVO = new HomeRespVO();
        //拿到菜单数据
        List<PermissionRespNodeVO> list = permissionService.permissionTreeList(userId);
        homeRespVO.setMenus(list);
        //拿到用户信息数据
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        UserInfoRespVO respVO = new UserInfoRespVO();
        if (sysUser != null) {
            respVO.setUsername(sysUser.getUsername());
            respVO.setDeptName("迎学教育");
            respVO.setId(sysUser.getId());
        }
        homeRespVO.setUserInfoVO(respVO);
        return homeRespVO;
    }

}
