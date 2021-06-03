package com.youlu.server.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * @Description
 * @author yangyang.duan
 * @date 2020/8/27
 */
@SpringBootApplication
@MapperScan("com.youlu.server.task.dao")
@EnableScheduling
public class Application {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
