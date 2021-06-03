package com.youlu.server.task.dao.mysql.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/17 11:16
 */
@Data
public class LabelFieldMappingDO implements Serializable {
    private Long id;
    private String reportName;
    private String srcFieldName;
    private String srcField;
    private String targetField;
}
