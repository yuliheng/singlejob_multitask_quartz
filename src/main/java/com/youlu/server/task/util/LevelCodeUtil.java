package com.youlu.server.task.util;

import com.alibaba.druid.util.StringUtils;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/11/29
 */
public class LevelCodeUtil {

    /**
     * @Description 获取某层级编码的父级编码
     * @author yangyang.duan
     * @date 2020/11/29
     */
    public static String getParent(String levelCode) {
        if(StringUtils.isEmpty(levelCode) || levelCode.length() < 20){
            return null;
        }
        return levelCode.substring(0,levelCode.length() - 10);
    }
}
