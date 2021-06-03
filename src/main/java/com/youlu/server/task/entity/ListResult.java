package com.youlu.server.task.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yangyang.duan
 * @Description 列表信息统计体
 * @date 2020/8/11
 */
@Data
public class ListResult<T> implements Serializable {
    //当前页
    private Integer page;
    //页容量
    private Integer pageSize;
    //总条数
    private Long totalCount;
    //总页数
    private Long totalPage;
    //列表内容
    private List<T> list;



    public static <T> ListResult<T> build(List<T> list,Long totalCount) {
        ListResult listResult = new ListResult<T>();
        listResult.setTotalCount(totalCount);
        listResult.setList(list);
        return listResult;
    }
}
