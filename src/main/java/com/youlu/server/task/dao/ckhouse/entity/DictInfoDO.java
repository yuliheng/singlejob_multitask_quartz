package com.youlu.server.task.dao.ckhouse.entity;

import lombok.Data;

/**
 * @author yangyang.duan
 * @Description 字典DO
 * @date 2020/11/29
 */
@Data
public class DictInfoDO {

    private String id;
    //名称
    private String name;
    //层级编码
    private String code;
    //父层级编码
    private String parentCode;
    //字典类型
    private String type;
}
