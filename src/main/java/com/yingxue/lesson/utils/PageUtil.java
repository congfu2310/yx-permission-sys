package com.yingxue.lesson.utils;

import com.github.pagehelper.Page;
import com.yingxue.lesson.vo.resp.PageVO;

import java.util.List;

public class PageUtil {

    // 私有构造方法，防止实例化
    private PageUtil(){}
    // 泛型方法，返回分页结果
    public static <T> PageVO<T> getPageVO(List<T> list){
        PageVO<T> result=new PageVO<>();
        if(list instanceof Page){
            Page<T> page= (Page<T>) list;
            result.setTotalRows(page.getTotal());
            result.setTotalPages(page.getPages());
            result.setPageNum(page.getPageNum());
            result.setCurPageSize(page.getPageSize());
            result.setPageSize(page.size());
            result.setList(page.getResult());
        }
        return result;
    }
}
