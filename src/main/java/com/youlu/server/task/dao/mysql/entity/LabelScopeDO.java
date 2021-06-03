package com.youlu.server.task.dao.mysql.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/15 10:10
 */
@Data
public class LabelScopeDO implements Serializable {
    private Long id;
    private String dptType;
    private String dptId;
    private Long labelId;
    private String labelscopeCreator;
    private Timestamp labelscopeCreateddate;
    private String labelscopeModifier;
    private Timestamp labelscopeModifieddate;
    private Integer labelscopeDelstatus;
}
