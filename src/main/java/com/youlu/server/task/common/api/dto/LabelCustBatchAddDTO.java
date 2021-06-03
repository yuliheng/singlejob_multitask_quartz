package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/18 17:00
 */
@Data
public class LabelCustBatchAddDTO implements Serializable {
    private String dataScope;
    private List<String> data;
    private List<LabelDTO> labelList;

}

