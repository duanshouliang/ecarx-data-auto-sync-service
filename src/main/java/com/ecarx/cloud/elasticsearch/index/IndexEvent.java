package com.ecarx.cloud.elasticsearch.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexEvent implements Serializable{
    /**
     * insert, update, delete
     */
    private String cmd;

    /**
     * 索引名
     */
    private String topic;

    private String type;
    /**
     *
     * 数据
     */
    private Map<String, Object> fields;

    /**
     *
     * update/delete的query条件
     */
    private Map<String, Object> condition;

    /**
     * 联合主键
     */
    private List<String> unionPrimaryKeys;
}
