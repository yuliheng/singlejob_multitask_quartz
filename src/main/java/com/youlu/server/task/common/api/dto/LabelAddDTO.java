package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/15 15:45
 */
@Data
public class LabelAddDTO implements Serializable {

    private String labelCategory;
//    private String labelParentlevelcode;
    private String labelName;
//    private String labelColor;
    private String labelMode;
//    private Integer labelSortnum;
//    private String labelTopstatus;
    private List<String> labelscopesDptids;
    private List<String> disableChildlabelids;
}
