package com.youlu.server.task.dao.mysql.entity;

import lombok.Data;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/5/7
 */
@Data
public class UserDO {

    private Long id;
    private String username;
    private String password;
}
