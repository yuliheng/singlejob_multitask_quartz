package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/14 17:24
 */
@Data
public class LabelTaskDTO implements Serializable {

    private String loginUserId;
    private String labelCategory;
    private String labelName;
    private String labelCreateType;
    private List<String> labelScope;
    private List<LabelConditionDTO> labelRules;
    private String taskName;
    private String cronExpression;
}
