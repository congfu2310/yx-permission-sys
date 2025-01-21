package com.yingxue.lesson.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.yingxue.lesson.constants.Constant;
import com.yingxue.lesson.entity.SysDept;
import com.yingxue.lesson.entity.SysUser;
import com.yingxue.lesson.exception.BusinessException;
import com.yingxue.lesson.exception.code.BaseResponseCode;
import com.yingxue.lesson.mapper.SysDeptMapper;
import com.yingxue.lesson.mapper.SysUserMapper;
import com.yingxue.lesson.service.DeptService;
import com.yingxue.lesson.service.RedisService;
import com.yingxue.lesson.service.UserService;
import com.yingxue.lesson.utils.CodeUtil;
import com.yingxue.lesson.utils.DataResult;
import com.yingxue.lesson.vo.req.DeptAddReqVO;
import com.yingxue.lesson.vo.req.DeptUpdateReqVO;
import com.yingxue.lesson.vo.resp.DeptRespNodeVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class DeptServiceImpl implements DeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserService userService;

    @Override
    public List<SysDept> selectAll() {
        //查询所有的部门数据
        List<SysDept> list = sysDeptMapper.selectAll();
        //遍历他们
        for (SysDept sysDept : list) {
            //拿到父级ID
            SysDept parent = sysDeptMapper.selectByPrimaryKey(sysDept.getPid());
            if (parent != null) {
                //拿到父级名称
                sysDept.setPidName(parent.getName());
            }
        }
        return list;
    }

    //拿到部门树形tree的实现
    @Override
    public List<DeptRespNodeVO> deptTreeList() {
        List<SysDept> list = sysDeptMapper.selectAll();
        //默认加一个顶级方便新增顶级部门
        DeptRespNodeVO respNodeVO = new DeptRespNodeVO();
        respNodeVO.setTitle("默认顶级部门");
        respNodeVO.setId("0");
        respNodeVO.setSpread(true);
        respNodeVO.setChildren(getTree(list));
        List<DeptRespNodeVO> result = new ArrayList<>();
        result.add(respNodeVO);
        return result;
    }


    //拿到tree列表
    private List<DeptRespNodeVO> getTree(List<SysDept> all) {
        List<DeptRespNodeVO> list = new ArrayList<>();
        for (SysDept sysDept : all) {
            if (sysDept.getPid().equals("0")) {
                DeptRespNodeVO deptTree = new DeptRespNodeVO();
                BeanUtils.copyProperties(sysDept, deptTree);
                deptTree.setTitle(sysDept.getName());
                deptTree.setSpread(true);
                deptTree.setChildren(getChild(sysDept.getId(), all));
                list.add(deptTree);
            }
        }
        return list;
    }

    //拿到children
    private List<DeptRespNodeVO> getChild(String id, List<SysDept> all) {
        List<DeptRespNodeVO> list = new ArrayList<>();
        for (SysDept sysDept : all) {
            if (sysDept.getPid().equals(id)) {
                DeptRespNodeVO deptTree = new DeptRespNodeVO();
                BeanUtils.copyProperties(sysDept, deptTree);
                deptTree.setTitle(sysDept.getName());
                deptTree.setChildren(getChild(sysDept.getId(), all));
                list.add(deptTree);
            }
        }
        return list;
    }

    @Override
    public SysDept addDept(DeptAddReqVO vo) {
        String relationCode;
        long result = redisService.incrby(Constant.DEPT_CODE_KEY, 1);
        String deptCode = CodeUtil.deptCode(String.valueOf(result), 6, "0");
        SysDept parent = sysDeptMapper.selectByPrimaryKey(vo.getPid());
        if (vo.getPid().equals("0")) {
            relationCode = deptCode;
        } else if (null == parent) {
            log.error("传入的 pid:{}不合法", vo.getPid());
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        } else {
            //最后的关系编码 = 父级关系编码+部门编码
            relationCode = parent.getRelationCode() + deptCode;
        }
        SysDept sysDept = new SysDept();
        BeanUtils.copyProperties(vo, sysDept);
        sysDept.setCreateTime(new Date());
        sysDept.setId(UUID.randomUUID().toString());
        sysDept.setDeptNo(deptCode);
        sysDept.setRelationCode(relationCode);
        int count = sysDeptMapper.insertSelective(sysDept);
        if (count != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        return sysDept;
    }


    @Override
    //通过事务管理（@Transactional），确保所有操作要么全部成功，要么全部回滚，以保证数据一致性
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(DeptUpdateReqVO vo) {
        //保存更新部门数据
        SysDept sysDept = sysDeptMapper.selectByPrimaryKey(vo.getId());
        if (null == sysDept) {
            log.error("传入 的 id:{}不合法", vo.getId());
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        SysDept update = new SysDept();
        BeanUtils.copyProperties(vo, update);
        update.setUpdateTime(new Date());
        int count = sysDeptMapper.updateByPrimaryKeySelective(update);
        if (count != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        //维护层级关系
        //如果父级ID不一样，说明层级发生了变化
        if (!vo.getPid().equals(sysDept.getPid())) {
            //子集部门的层级关系编码=父级部门层级关系编码+本身部门编码
            SysDept parent = sysDeptMapper.selectByPrimaryKey(vo.getPid());
            //获取新的父级部门信息，并检查其合法性
            if (!vo.getPid().equals("0") && null == parent) {
                log.error("传入 的 pid:{}不合法", vo.getId());
                throw new BusinessException(BaseResponseCode.DATA_ERROR);
            }
            //拿到新旧层级编码
            SysDept oldParent = sysDeptMapper.selectByPrimaryKey(sysDept.getPid());
            String oldRelationCode;
            String newRelationCode;
            //如果原来的父级ID为 0，则当前部门是根目录，新的层级关系编码为新父级部门的层级关系编码加上当前部门编码
            if (sysDept.getPid().equals("0")) {
                oldRelationCode = sysDept.getDeptNo();
                newRelationCode = parent.getRelationCode() + sysDept.getDeptNo();
            } else if (vo.getPid().equals("0")) {
                //如果新的父级ID为 0，则当前部门升级为根目录，新的层级关系编码为当前部门编码。
                oldRelationCode = sysDept.getRelationCode();
                newRelationCode = sysDept.getDeptNo();
            } else {
                //如果是普通的目录层级变化，新的层级关系编码为新父级部门的层级关系编码
                oldRelationCode = oldParent.getRelationCode();
                newRelationCode = parent.getRelationCode();
            }
            //更新部门及其子部门的层级关系编码
            sysDeptMapper.updateRelationCode(oldRelationCode, newRelationCode, sysDept.getRelationCode());
        }
    }

    @Override
    public void deleted(String id) {
        SysDept sysDept = sysDeptMapper.selectByPrimaryKey(id);
        if (null == sysDept) {
            log.error("传入 的 id:{}不合法", id);
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        List<String> deptIds = sysDeptMapper.selectChildIds(sysDept.getRelationCode());
        //判断它和它子集的叶子节点是否关联有用户
        List<SysUser> list = userService.selectUserInfoByDeptIds(deptIds);
        if (!list.isEmpty()) {
            throw new BusinessException(BaseResponseCode.NOT_PERMISSION_DELETED_DEPT);
        }
        //逻辑删除部门
        sysDept.setDeleted(0);
        sysDept.setUpdateTime(new Date());
        int count = sysDeptMapper.updateByPrimaryKeySelective(sysDept);
        if (count != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }


}
