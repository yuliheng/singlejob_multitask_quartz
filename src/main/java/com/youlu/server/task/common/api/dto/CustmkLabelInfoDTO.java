package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/20 11:07
 */
@Data
public class CustmkLabelInfoDTO implements Serializable {
    private String labelId;
    private String labelCode;
    private String labelName;
    private String labelCategory;
    private String labelColor;
    private String labelMode;
    private String labelSortnum;
    private String labelTopstatus;
    private String labelDelstatus;
    private String labelCreator;
    private String labelCreatorName;
    private String labelCreateddate;
    private String labelModifier;
    private String labelModifieddate;
    private List<LabelScopes> labelScopes;

    @Data
    class LabelScopes implements Serializable {
        private String labelscopeId;
        private String labelId;
        private String dptType;
        private String dptId;
        private String dptName;
    }

}


