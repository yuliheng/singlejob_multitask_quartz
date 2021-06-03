package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/22 17:57
 */
@Data
public class LabelParams implements Serializable {
    private String labelId;
    private String custmkLabelId;
}
