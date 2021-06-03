package com.youlu.server.task.entity;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/11/29
 */
public class ConstantValue {

    /**
     * 字典类型常量
     * A代表大区 B分校 C获客方式 D部门 E推广账号  F推广人 P推广项目 L订单类型  LY订单来源  TN渠道类型/渠道名称
     */
    public static final String DICT_TYPE_AREA = "A";
    public static final String DICT_TYPE_SCHOOL = "B";
    public static final String DICT_TYPE_GETCUSTOM = "C";
    public static final String DICT_TYPE_DPT = "D";
    public static final String DICT_TYPE_PROMOTEACCOUNT = "E";
    public static final String DICT_TYPE_PROMOTEPERSON = "F";
    public static final String DICT_TYPE_PROJECT = "P";
    public static final String DICT_TYPE_ORDERTYPE = "L";
    public static final String DICT_TYPE_ORDERSOURCE = "LY";
    public static final String DICT_TYPE_CHANNELTYPE = "T";
    public static final String DICT_TYPE_CHANNELNAME = "NN";
    public static final String DICT_TYPE_SCHOOL2 = "S";


    public static final String MONTH = "month";
    public static final String WEEK = "week";
    public static final String DAY = "day";

    /**
     * 分组类型
     */
    public static final String TYPE_SCHOOL = "school";
    public static final String TYPE_PROJECT = "project";
    public static final String TYPE_CHANNEL = "channel";


    /**
     * JWT相关常量
     */
    public static final String JWT_ERRCODE_EXPIRE = "jwt_errcode_expire";
    public static final String JWT_ERRCODE_FAIL = "jwt_errcode_fail";
    public static final String JWT_SECERT = "youloo2020bigdata_jwtyouloo2020bigdata_jwtyouloo2020bigdata_jwt";
    public static final Long JWT_TTL = 1000L * 3600;

}
