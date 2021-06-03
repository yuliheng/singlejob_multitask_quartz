//package com.youlu.server.task.config.webmvc;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * @author yangyang.duan
// * @Description
// * @date 2021/5/7
// */
////@Configuration
//public class WebMvcConfig implements WebMvcConfigurer {
//
//    @Autowired
//    private  JwtHandlerInterceptor jwtHandlerInterceptor;
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(jwtHandlerInterceptor)
//                .addPathPatterns("/**")
//                .excludePathPatterns("/user/login");
//    }
//}
