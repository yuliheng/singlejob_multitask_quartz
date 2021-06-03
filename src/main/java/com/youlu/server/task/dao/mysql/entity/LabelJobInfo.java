package com.youlu.server.task.dao.mysql.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/20 11:25
 */
@Data
public class LabelJobInfo implements Serializable {
    private String labelId;
    private String custmkLabelId;
    private String labelName;
    private String jobProcessStatus;
}
