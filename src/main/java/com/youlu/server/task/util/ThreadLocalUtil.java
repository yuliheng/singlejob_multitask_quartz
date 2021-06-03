package com.youlu.server.task.util;

import java.util.Calendar;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/12/2
 */
public class ThreadLocalUtil {

    public static ThreadLocal<Calendar> calendarThreadLocal = new ThreadLocal<>();
}
