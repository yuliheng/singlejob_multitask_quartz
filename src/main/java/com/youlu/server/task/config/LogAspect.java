package com.youlu.server.task.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.text.MessageFormat;


/**
 * @author yangyang.duan
 * @Description 日志切面
 * @date 2020/8/11
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("execution(* com.youlu.server.task..service..*ServiceImpl.*(..)) || execution(* com.youlu.server.task..dao..*Mapper.*(..))")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        final Object target = point.getTarget();
        final Signature signature = point.getSignature();
        final Object[] args = point.getArgs();
        if(!(signature instanceof MethodSignature)){
            return point.proceed(args);
        }
        final Method method = ((MethodSignature) signature).getMethod();
        log.info("class:{}, method:{}, args:{}", target.getClass().getName(), method.getName(), JSON.toJSONString(args));
        long startTime = System.currentTimeMillis();
        try {
            Object result = point.proceed(args);
            long endTime = System.currentTimeMillis();
            log.info("class:{}, method:{}, result:{}, spend:{}", target.getClass().getName(), method.getName(), JSON.toJSONString(result), endTime - startTime);
            return result;
        } catch (Throwable throwable) {
            String message = MessageFormat.format("class:{0}, method:{1}, args:{2}",target.getClass().getName(), method.getName(), JSON.toJSONString(args));
            log.error(message, throwable);
            throw throwable;
        }
    }

}
