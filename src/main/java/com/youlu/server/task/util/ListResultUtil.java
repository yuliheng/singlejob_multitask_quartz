package com.youlu.server.task.util;



import com.youlu.server.task.entity.ListResult;

import java.util.List;
import java.util.function.Function;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/8/12
 */
public class ListResultUtil {

    /**
     * @Description 将E的List转化为列表对象（升级版）
     * @author yangyang.duan
     * @date 2020/11/29
     */
//    public static <E,T extends ListResult<E>> T fill2(List<E> vos, Integer page, Integer pageSize, Long totalCount) {
//        T t = (T) new ListResult<E>();
//        t.setList(vos);
//        t.setPage(page);
//        t.setPageSize(pageSize);
//        t.setTotalCount(totalCount);
//        Long totalPage = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
//        t.setTotalPage(totalPage);
//        return t;
//    }

    /**
     * @Description 将E的List转化为列表对象
     * @author yangyang.duan
     * @date 2020/11/29
     */
    public static <E> ListResult<E> fill(List<E> vos, Integer page, Integer pageSize, Long totalCount) {
        ListResult<E> listResult = new ListResult<>();
        listResult.setList(vos);
        listResult.setPage(page);
        listResult.setPageSize(pageSize);
        listResult.setTotalCount(totalCount);
        Long totalPage = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        listResult.setTotalPage(totalPage);
        return listResult;
    }

    /**
     * @Description 把K类型的列表对象转化为V类型的列表对象
     * @author yangyang.duan
     * @date 2020/11/29
     */
    public static <K,V> ListResult<V> convert(ListResult<K> kListResult, Function<List<K>,List<V>> function) {
        List<K> kList = kListResult.getList();
        List<V> vList = function.apply(kList);
        return fill(vList, kListResult.getPage(), kListResult.getPageSize(), kListResult.getTotalCount());
    }
}
