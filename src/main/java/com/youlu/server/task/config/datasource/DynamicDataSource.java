package com.youlu.server.task.config.datasource;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/8/27
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.METHOD , ElementType.TYPE } )
public @interface DynamicDataSource {

    @AliasFor( "dataSource" )
    String value () default StringUtils.EMPTY;

    @AliasFor( "value" )
    String dataSource () default StringUtils.EMPTY;

}
