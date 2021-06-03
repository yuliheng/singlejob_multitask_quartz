package com.youlu.server.task.controller.vo;

import lombok.Data;

import java.util.List;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/5/8
 */
@Data
public class UserInfoVO {
    private String name;
    private List<String> roles;
    private String introduction;
    private String avatar;
}
