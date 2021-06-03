package com.youlu.server.task.entity;

import lombok.Data;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/5/12
 */
@Data
public class UserInfo {
    private String username;
    private String password;
    private String token;
}
