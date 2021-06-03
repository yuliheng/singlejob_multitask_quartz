package com.youlu.server.task.controller.vo;

import lombok.Data;

/**
 * @author yangyang.duan
 * @Description 返回值基本结构体
 * @date 2020/8/11
 */
@Data
public class ResultVO<T> {
    //请求接口的API路径
    private String api;
    //返回码
    private String result = "000000";
    //网关需要的状态码(自定义的)
    private String code = "0000";
    //提示信息
    private String msg;
    //返回体
    private T data;
    //流水号
    private String seqno;
    //客户端流水号
    private String cid;
    //时间戳
    private String timestamp;


    /** 成功*/
    public static<T> ResultVO<T> OK(T data,String msg) {
        ResultVO<T> tResultVO = new ResultVO<>();
        tResultVO.setData(data);
        tResultVO.setMsg(msg);
        return tResultVO;
    }

    /** 失败*/
    public static ResultVO<Object> ERROR(String msg) {
        ResultVO<Object> rv = new ResultVO<>();
        rv.setMsg(msg);
        return rv;
    }
    /**
     *
     * @param message
     * @return
     */
    public static ResultVO<?> SUCCESS(String message) {
        ResultVO<Object> rv = new ResultVO<>();
        rv.msg = message;
//        rv.code = 200;
        return rv;
    }




}
