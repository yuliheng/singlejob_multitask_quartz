package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/14 17:42
 */
@Data
public class LabelInfoDTO implements Serializable {

    private String labelId;
    private String custmkLabelId;
    private String labelCategory;
    private String labelName;
    private String labelCreateType;
    private List<CustmkLabelInfoDTO.LabelScopes> labelScopes;
    private String labelCreator;
    private String labelCreateDate;
    private String taskExecuteStatus;
}
