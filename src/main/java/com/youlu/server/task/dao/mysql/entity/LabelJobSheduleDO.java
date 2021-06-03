package com.youlu.server.task.dao.mysql.entity;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/15 10:35
 */
@Data
public class LabelJobSheduleDO implements Serializable {
    private Long id;
    private String jobName;
    private String jobCronExpression;
    private String jobParameter;
    private String qrtzJobName;
    private String qrtzTriggerName;
    private Long labelId;
    private Timestamp jobFirsttime;
    private Timestamp jobLasttime;
    private String jobDuration;
    private Integer jobProcessStatus;
    private String jobCreator;
    private Timestamp jobCreateddate;
    private String jobModifier;
    private Timestamp jobModifieddate;
    private Integer jobStatus;
}
