package com.youlu.server.task.common.constant;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/5/20 17:28
 */
public enum HeaderEnum {

    ACCEPT("accept"),
    AUTHORIZATION("authorization"),
    CONTENTTYPE("Content-Type"),
    CHECKCODE("checkCode"),
    URL("url");

    private String value;

    HeaderEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
