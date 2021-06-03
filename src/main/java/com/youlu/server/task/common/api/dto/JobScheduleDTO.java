package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/18 17:31
 */
@Data
public class JobScheduleDTO implements Serializable {
    private String labelSQL;
    private String jobScheduleId;
    private String gateway;
    private String remoteInterfaceLabelId;
    private String remoteInterfaceLabelName;

}
