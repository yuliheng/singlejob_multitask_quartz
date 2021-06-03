package com.youlu.server.task.config;

import com.youlu.server.task.controller.vo.ResultVO;
import com.youlu.server.task.util.ResultVOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author yangyang.duan
 * @Description 全局controller异常处理
 * @date 2020/8/11
 */
@Slf4j
@ControllerAdvice
public class ControllerExceptionAdvice {


    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public ResultVO handle(Exception e) {
        log.error("common exception", e);
        ResultVO resultVO = ResultVOUtil.error("10000", e.getMessage());
        return resultVO;
    }

//    // 捕捉UnauthorizedException
//    @ResponseBody
//    @ResponseStatus(HttpStatus.UNAUTHORIZED)
//    @ExceptionHandler(UnauthorizedException.class)
//    public ResultVO handle401(UnauthorizedException ex) {
//        ResultVO resultVO = ResultVOUtil.error(HttpStatusEnum.UNAUTHORIZED.code(), "操作失败，权限不足，请联系管理员");
//        return resultVO;
//    }

}
