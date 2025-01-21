package com.yingxue.lesson.service.impl;

import com.github.pagehelper.PageHelper;
import com.yingxue.lesson.entity.SysLog;
import com.yingxue.lesson.mapper.SysLogMapper;
import com.yingxue.lesson.service.LogService;
import com.yingxue.lesson.utils.PageUtil;
import com.yingxue.lesson.vo.req.SysLogPageReqVO;
import com.yingxue.lesson.vo.resp.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private SysLogMapper sysLogMapper;

    @Override
    public PageVO<SysLog> pageInfo(SysLogPageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());
        List<SysLog> sysLogs = sysLogMapper.selectAll(vo);
        return PageUtil.getPageVO(sysLogs);
    }


    @Override
    public int deleted(List<String> logIds) {
        return sysLogMapper.batchDeletedLog(logIds);
    }
}
