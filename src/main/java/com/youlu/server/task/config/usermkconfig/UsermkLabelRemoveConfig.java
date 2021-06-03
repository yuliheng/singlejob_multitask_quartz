package com.youlu.server.task.config.usermkconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/20 16:50
 */
@Component
@ConfigurationProperties(prefix = "usermk.labelremove")
@Data
public class UsermkLabelRemoveConfig {
    private String accept;
    private String authorization;
    private String ContentType;
    private String checkCode;
    private String url;
}
