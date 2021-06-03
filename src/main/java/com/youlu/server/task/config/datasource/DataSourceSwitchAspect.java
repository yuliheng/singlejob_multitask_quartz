package com.youlu.server.task.config.datasource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author yangyang.duan
 * @Description 通过AOP的方式来切换数据源
 * @date 2020/8/27
 */
@Aspect
@Order(1)
@Component
public class DataSourceSwitchAspect {


    @Before("execution(* com.youlu.server.task..dao.mysql.*Mapper.*(..))")
    public void mysqlMapper ( JoinPoint joinPoint ) {
        setDataSourceKey(joinPoint, DataSourceConfig.DATA_SOURCE_MYSQL);
    }

    @Before( "execution(* com.youlu.server.task..dao.ckhouse.*Mapper.*(..))" )
    public void ckHouseMapper ( JoinPoint joinPoint ) {
        setDataSourceKey(joinPoint, DataSourceConfig.DATA_SOURCE_CKHOUSE);
    }
    @Before( "execution(* com.youlu.server.task..dao.sqlserver.*Mapper.*(..))" )
    public void sqlServerMapper ( JoinPoint joinPoint ) {
        setDataSourceKey(joinPoint, DataSourceConfig.DATA_SOURCE_SQLSERVER);
    }

    private void setDataSourceKey (JoinPoint joinPoint, final String defaultKey) {
        final Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        final DynamicDataSource dynamicDataSource = method.getAnnotation(DynamicDataSource.class);
        if (Objects.isNull( dynamicDataSource )) {
            DynamicMultipleDataSource.setDataSourceKey(defaultKey);
            return;
        }
        DynamicMultipleDataSource.setDataSourceKey(dynamicDataSource.value());
    }

}
