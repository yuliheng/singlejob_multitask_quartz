//package com.youlu.server.task.config.webmvc;
//
//import com.youlu.server.task.entity.JwtCheckResult;
//import com.youlu.server.task.entity.UserInfo;
//import com.youlu.server.task.service.UserService;
//import com.youlu.server.task.util.JwtUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
///**
// * @author yangyang.duan
// * @Description
// * @date 2021/5/7
// */
//@Slf4j
//@Component
//public class JwtHandlerInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private UserService userService;
//
//    /**
//     * @Description 请求前拦截：判断jwt的token有效性
//     * @author yangyang.duan
//     * @date 2021/5/7
//     */
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String token = request.getHeader("X-Token");
////        String token = request.getParameter("token");
//        //校验token合法性
//        JwtCheckResult checkResult = JwtUtil.validateJWT(token);
//        if (checkResult.getSuccess()){
//            //校验token是否还在使用
//            UserInfo userInfo = userService.getUserInfo(token);
//            if(userInfo != null){
//                log.info("JwtHandlerInterceptor preHandle success with token:{}",token);
//                return true;
//            }
//            log.info("JwtHandlerInterceptor token validate success but user not available, with token:{}",token);
//            return false;
//        }else {
//            log.warn("JwtHandlerInterceptor preHandle false with token:{}",token);
//            return false;
//        }
//    }
//}
