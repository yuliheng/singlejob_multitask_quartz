package com.youlu.server.task.common.quarts.defdatasource;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.Data;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/18 14:27
 */
@Data
public class SelfDataSource {

    private DataSource cataSourceCH = null;
    private DataSource cataSourceMYSQL = null;
    private static Boolean flag = false; // dev:false ; prod:true;

    //    private CHDataSource() {
//
//    }
    private static class Singleton {

        private static SelfDataSource chDataSource = null;

        static {
            chDataSource = new SelfDataSource();
            try {
                Map<String, String> map = new HashMap();
                map.put("driverClassName", "ru.yandex.clickhouse.ClickHouseDriver");
                if (!flag) {
                    map.put("url", "jdbc:clickhouse://192.168.11.182:8123/ods");
                } else {
                    map.put("url", "jdbc:clickhouse://172.31.4.50:8123/ods");
                }
                map.put("username", "");
                map.put("password", "");
                chDataSource.cataSourceCH = DruidDataSourceFactory.createDataSource(map);

                map = new HashMap();
                map.put("driverClassName", "com.mysql.cj.jdbc.Driver");
                if (!flag) {
                    map.put("url", "jdbc:mysql://192.168.11.253:3306/platform?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2b8&useSSL=true&tinyInt1isBit=false");
                } else {
                    map.put("url", "jdbc:clickhouse://172.31.4.50:8123/ods");
                }
                map.put("username", "root");
                map.put("password", "123456");
                chDataSource.cataSourceMYSQL = DruidDataSourceFactory.createDataSource(map);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static SelfDataSource getInstance() {
            return chDataSource;
        }
    }

    public static SelfDataSource getInstance() {
        return Singleton.getInstance();
    }

    public static SelfDataSource init() {
        return getInstance();
    }


}
