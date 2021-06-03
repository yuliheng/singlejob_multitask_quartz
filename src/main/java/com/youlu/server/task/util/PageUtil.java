package com.youlu.server.task.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/12/2
 */
public class PageUtil {

    /**
     * @Description 伪分页
     * @author yangyang.duan
     * @date 2020/12/2
     */
    public static <E> List<E> fakePage(List<E> list, Integer page, Integer pageSize){
        int totalCount = list.size();
        int beginIndex = (page - 1) * pageSize;
        int endIndex = page * pageSize;
        if(beginIndex > totalCount || endIndex < 0){
            return new ArrayList<>();
        }else {
            int bIndex = beginIndex < 0 ? 0 : beginIndex;
            int eIndex = endIndex > totalCount ? totalCount : endIndex;
            return list.subList(bIndex, eIndex);
        }
    }
}
