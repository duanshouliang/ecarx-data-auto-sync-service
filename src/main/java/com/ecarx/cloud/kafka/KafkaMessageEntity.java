package com.ecarx.cloud.kafka;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class KafkaMessageEntity implements Serializable {

    private String instanceName;
    private String schemaName;
    private String tableName;
    private String eventType;
    private JSONObject newRow;
    private JSONObject oldRow;
    private Date operateTime;
    private List<String> primaryKeys;
}
