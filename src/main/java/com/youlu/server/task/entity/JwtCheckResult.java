package com.youlu.server.task.entity;

import io.jsonwebtoken.Claims;
import lombok.Data;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/5/7
 */
@Data
public class JwtCheckResult {

    private Boolean success;
    private Claims claims;
    private String ErrCode;
}
