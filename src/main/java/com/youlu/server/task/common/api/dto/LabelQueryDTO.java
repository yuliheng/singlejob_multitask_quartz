package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/18 16:22
 */
@Data
public class LabelQueryDTO implements Serializable {

    private String labelCreator;
    private String labelName;
    private String labelCategory;
    private String labelMode;
    private List<String> schoolIdList;

}
