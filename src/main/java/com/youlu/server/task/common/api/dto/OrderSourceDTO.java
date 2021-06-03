package com.youlu.server.task.common.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author ylh
 * @version 1.0
 * @date 2021/6/2 9:45
 */
@Data
public class OrderSourceDTO implements Serializable {
    private List<OrderSourceFirstLev> PlatformAppOptions;

    @Data
    class OrderSourceFirstLev implements Serializable {

        private String PlatformCode;
        private String PlatformName;
        private List<OrderSourceSecondLev> PlatformApps;

        @Data
        class OrderSourceSecondLev implements Serializable {
            private String Name;
            private String Code;


        }
    }

}
