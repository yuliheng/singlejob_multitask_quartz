package com.youlu.server.task.dao.mysql.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/15 9:41
 */
@Data
public class LabelInfoDO implements Serializable {
    private Long id;
    private String custmkLabelId;
    private String labelCategory;
    private String labelName;
    private String labelMode;
    private String labelCondition;
    private String labelCreator;
    private Timestamp labelCreateddate;
    private String labelModifier;
    private Timestamp labelModifieddate;
    private Integer labelDelstatus;
    private Integer labelAavlstatus;
}
