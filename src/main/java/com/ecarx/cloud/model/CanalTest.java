package com.ecarx.cloud.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class CanalTest implements Serializable {
    private Integer id;
    private String listenerTable;
    private Integer mysqlPort;
    private String mysqlHost;
    private String instanceName;
    private String mysqlUsername;
    private String mysqlPassword;
}
