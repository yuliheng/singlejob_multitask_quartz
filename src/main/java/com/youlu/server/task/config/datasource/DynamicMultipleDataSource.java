package com.youlu.server.task.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author yangyang.duan
 * @Description
 * @date 2020/8/27
 */
public class DynamicMultipleDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> DATA_SOURCE_KEY = new ThreadLocal<>();

    static void setDataSourceKey (String dataSource) {
        DATA_SOURCE_KEY.set(dataSource);
    }

    private static void clear() {
        DATA_SOURCE_KEY.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        final String lookupKey = DATA_SOURCE_KEY.get();
        clear();
        return lookupKey;
    }
}
