package com.youlu.server.task.config.datasource;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/8/27
 */
@Configuration
public class DataSourceConfig {

    public static final String DATA_SOURCE_MYSQL = "mysqlDataSource";
    public static final String DATA_SOURCE_CKHOUSE = "ckhouseDataSource";
    public static final String DATA_SOURCE_SQLSERVER = "sqlserverDataSource";

    @Bean
    public DynamicMultipleDataSource dynamicMultipleDataSource(@Qualifier(DATA_SOURCE_MYSQL) DataSource mysqlDataSource,
                                                               @Qualifier(DATA_SOURCE_CKHOUSE) DataSource ckhouseDataSource, @Qualifier(DATA_SOURCE_SQLSERVER) DataSource sqlServerDataSource) {
        DynamicMultipleDataSource dynamicMultipleDataSource = new DynamicMultipleDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DATA_SOURCE_MYSQL, mysqlDataSource);
        targetDataSources.put(DATA_SOURCE_CKHOUSE, ckhouseDataSource);
        targetDataSources.put(DATA_SOURCE_SQLSERVER, sqlServerDataSource);
        dynamicMultipleDataSource.setTargetDataSources(targetDataSources);
        dynamicMultipleDataSource.setDefaultTargetDataSource(mysqlDataSource);
        return dynamicMultipleDataSource;
    }

    @Primary
    @Bean(name = DATA_SOURCE_MYSQL)
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public DataSource mysqlDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = DATA_SOURCE_CKHOUSE)
    @ConfigurationProperties(prefix = "spring.datasource.ckhouse")
    public DataSource ckhouseDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean(name = DATA_SOURCE_SQLSERVER)
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")
    public DataSource sqlServerDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

//    @Bean
//    public SqlSessionFactory sqlSessionFactory (DynamicMultipleDataSource dynamicMultipleDataSource) throws Exception {
//        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//        sqlSessionFactoryBean.setDataSource(dynamicMultipleDataSource);
//        return sqlSessionFactoryBean.getObject();
//    }
//
//    @Bean
//    public DataSourceTransactionManager transactionManager (DynamicMultipleDataSource dynamicMultipleDataSource) throws Exception {
//        return new DataSourceTransactionManager(dynamicMultipleDataSource);
//    }


}
