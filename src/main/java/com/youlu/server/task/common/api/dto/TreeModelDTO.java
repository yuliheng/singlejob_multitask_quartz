package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/13 15:58
 */
@Data
public class TreeModelDTO implements Serializable {

    private String id;
    private String code;
    private String value;
    private List<TreeModelDTO> children;
}
