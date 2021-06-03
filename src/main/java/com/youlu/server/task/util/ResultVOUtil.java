package com.youlu.server.task.util;


import com.youlu.server.task.controller.vo.ResultVO;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/8/11
 */
public class ResultVOUtil {

    public static <T> ResultVO success(T data){
        ResultVO<T> resultVO = new ResultVO<>();
        resultVO.setData(data);
        return resultVO;
    }

    public static ResultVO error(String code, String msg){
        ResultVO<Object> resultVO = new ResultVO<>();
        resultVO.setMsg(msg);
        resultVO.setCode(code);
        resultVO.setResult(code);
        return resultVO;
    }

}
