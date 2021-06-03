package com.youlu.server.task.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/12/3
 */
public class DecimalFormatUtil {

    private static List<String> patterns = Arrays.asList("#.0","#.00","#.000");
    private static Map<String, DecimalFormat> dfMap = new HashMap<>();

    static {
        patterns.forEach(pattern ->{
            dfMap.put(pattern,new DecimalFormat(pattern));
        });
    }

    public static String formatWithPattern(Double value, String pattern){
        DecimalFormat df = dfMap.get(pattern);
        if(df == null){
            throw new IllegalArgumentException("can not find the df according the your pattern !");
        }
        return df.format(value);
    }

}
