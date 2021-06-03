package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/20 10:42
 */
@Data
public class LabelQueryVO implements Serializable {
    private String labelCreator;
    private String labelCreatordatestart;
    private String labelCreatordateend;
    private Integer pageSize;
    private Integer pageIndex;
}
