package com.youlu.server.task.dao;

import com.youlu.server.task.util.DateUtil;

import java.util.Date;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/12/7
 */
public class DateUtilTest {

    public static void main(String[] args) {
        new Thread(() -> {
            for(int i = 0 ; i < 100; i++){
                testStringToDate();
            }
        }).start();

        new Thread(() -> {
            for(int i = 0 ; i < 100; i++){
                testDateToString();
            }
        }).start();

        testStringToDate();
        testDateToString();
    }




    public static void testStringToDate(){
        Date date = DateUtil.stringToDate("2020-10-10 10:10:10");
        String name = Thread.currentThread().getName();
        System.out.println(name +": "+ date);
    }

    public static void testDateToString(){
        Date date = new Date(System.currentTimeMillis());
        String dateStr = DateUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss");
        String name = Thread.currentThread().getName();
        System.out.println(name +": "+ dateStr);
    }
}
