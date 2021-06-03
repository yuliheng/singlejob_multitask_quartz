package com.youlu.server.task.util;

import com.alibaba.druid.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/8/13
 */
@Slf4j
public class DateUtil {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String DATE_PATTERN_1 = "yyyyMMdd";
    public static final String HOUR_PATTERN = "HH";
    public static final String MINUTE_PATTERN = "mm";
    public static final String SECOND_PATTERN = "ss";
    /**
     * @Description 每个线程使用自己的一个或几个SimpleDateFormat，避免频繁压栈出栈重复创造大对象
     * @author yangyang.duan
     * @date 2020/8/15
     */
    private static final ThreadLocal<Map<String, SimpleDateFormat>> sdfMap = new ThreadLocal<>();
    private static final List<String> sdfPatterns = Arrays.asList(DATE_TIME_PATTERN, DATE_PATTERN, DATE_PATTERN_1, HOUR_PATTERN, MINUTE_PATTERN, SECOND_PATTERN);
    public static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss_SSS");


//    private static final Map<String,SimpleDateFormat> sdfMap1 = new HashMap<>();

//    static {
//        sdfPatterns.forEach(pattern ->{
//            sdfMap1.put(pattern,new SimpleDateFormat(pattern));
//        });
//    }

//    private static SimpleDateFormat getSdf(String pattern){
//        return sdfMap1.get(pattern);
//    }

    /**
     * @Description 懒加载的方式来创造sdfMap
     * @author yangyang.duan
     * @date 2020/12/7
     */
    private static SimpleDateFormat getSdf(String pattern){
        Map<String, SimpleDateFormat> currentThreadSdfMap = sdfMap.get();
        if(currentThreadSdfMap == null){
            currentThreadSdfMap = new HashMap<>();
            sdfMap.set(currentThreadSdfMap);
        }
        SimpleDateFormat sdf = currentThreadSdfMap.get(pattern);
        if(sdf == null){
            if(sdfPatterns.contains(pattern)){
                sdf = new SimpleDateFormat(pattern);
                currentThreadSdfMap.put(pattern,sdf);
            }
        }
        if(sdf == null){
            throw new RuntimeException("cannot find simpleDateFromat according to pattern !");
        }
        return sdf;
    }

    /**
     * @Description 2020-08-28T03:35:50.000+0000 --> 2020-08-28 03:35:50
     * @author yangyang.duan
     * @date 2020/9/9
     */
    public static Date dateConvert(Date date) {
        return stringToDate(dateToString(date));
    }

    /**
     * @Description "2020-09-09 18:27:00" -->  2020-09-09 18:27:00
     * @author yangyang.duan
     * @date 2020/9/9
     */
    public static Date stringToDate(String dateStr) {
        if(StringUtils.isEmpty(dateStr)){
            return null;
        }
        SimpleDateFormat sdf = getSdf(DATE_TIME_PATTERN);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            log.error("exception:{}",e);
        }
        return null;
    }

    /**
     * @Description 2020-09-09 18:27:00 --> "2020-09-09 18:27:00"
     * @author yangyang.duan
     * @date 2020/9/9
     */
    public static String dateToString(Date date){
        if(date == null){
            return null;
        }
        SimpleDateFormat sdf = getSdf(DATE_TIME_PATTERN);
        return sdf.format(date);
    }

    /**
     * @Description 根据指定的pattern将字符串转化为Date
     * @author yangyang.duan
     * @date 2020/9/9
     */
    public static Date stringToDate(String dateStr,String pattern) {
        if(StringUtils.isEmpty(dateStr)){
            return null;
        }
        SimpleDateFormat sdf = getSdf(pattern);

        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            log.error("exception:{}",e);
        }
        return null;
    }

    /**
     * @Description 根据指定的pattern将Date转化为字符串
     * @author yangyang.duan
     * @date 2020/9/9
     */
    public static String dateToString(Date date,String pattern){
        SimpleDateFormat sdf = getSdf(pattern);
        return sdf.format(date);
    }


    /**
     * @Description 2020-09-09  --> 20200909L, 2020-09-09 18:27:00 --> 20200909L
     * @author yangyang.duan
     * @date 2020/9/9
     */
    public static Long toYYYYMMDD(Date date){
        if(date == null){
            return null;
        }
        SimpleDateFormat sdf = getSdf("yyyyMMdd");
        String dateStr = sdf.format(date);
        return Long.valueOf(dateStr);
    }

}
