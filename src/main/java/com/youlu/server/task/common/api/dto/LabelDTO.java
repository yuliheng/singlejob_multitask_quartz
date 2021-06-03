package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LabelDTO implements Serializable {
    private String id;
    private String name;
}
