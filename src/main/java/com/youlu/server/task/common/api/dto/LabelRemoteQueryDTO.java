package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/20 10:42
 */
@Data
public class LabelRemoteQueryDTO implements Serializable {
    private String labelCreator;
    private String labelName;
    private String labelCreatordatestart;
    private String labelCreatordateend;
}
