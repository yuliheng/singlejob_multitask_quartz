package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/17 11:36
 */
@Data
public class LabelConditionDTO implements Serializable {
    private String name;
    private String relation;
    private String value;
}
