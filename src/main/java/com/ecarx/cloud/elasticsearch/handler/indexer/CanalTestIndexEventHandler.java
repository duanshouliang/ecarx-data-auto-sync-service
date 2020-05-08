package com.ecarx.cloud.elasticsearch.handler.indexer;

import com.alibaba.fastjson.JSONObject;
import com.ecarx.cloud.elasticsearch.event.IndexEvent;
import com.ecarx.cloud.enumeration.IndexEventEnum;
import com.ecarx.cloud.kafka.KafkaMessageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanalTestIndexEventHandler extends IndexEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanalTestIndexEventHandler.class);
    private static final String TOPIC = "canal_test";

    @Override
    public IndexEvent handle(KafkaMessageEntity entity) {
        String cmd = entity.getEventType();
        IndexEvent indexEvent = new IndexEvent();
        indexEvent.setCmd(cmd);
        if(cmd.equalsIgnoreCase(IndexEventEnum.ADD.getValue())){
            this.handleAdd(indexEvent, entity);
        }else if(cmd.equalsIgnoreCase(IndexEventEnum.UPDATE.getValue())){
            try {
                this.handleUpdate(indexEvent, entity);
            } catch (Exception e) {
                LOGGER.error("Update operate with exception {}", e.getMessage());
                return null;
            }
        }else if(cmd.equalsIgnoreCase(IndexEventEnum.DELETE.getValue())){
            try {
                this.handleDelete(indexEvent, entity);
            } catch (Exception e) {
                LOGGER.error("Delete operate with exception {}", e.getMessage());
                return null;
            }
        }
        indexEvent.setTopic(TOPIC);
        indexEvent.setType("test");
        return indexEvent;
    }

    private void handleAdd(IndexEvent event, KafkaMessageEntity entity){
        event.setFields(entity.getNewRow());
    }
    private void handleUpdate(IndexEvent event, KafkaMessageEntity entity)throws Exception{
        List<String> primaryKeys = entity.getPrimaryKeys();
        if(null == primaryKeys || primaryKeys.size() == 0){
            throw new Exception("Table " + entity.getSchemaName() +"." + entity.getTableName() + "must have primary key");
        }
        String primaryKey = primaryKeys.get(0);
        event.setUnionPrimaryKeys(primaryKeys);
        JSONObject oldData = entity.getOldRow();
        Map<String, Object> condition = new HashMap<>();
        condition.put(primaryKey, oldData.get(primaryKey));
        event.setCondition(condition);
        event.setFields(entity.getNewRow());
    }

    private void handleDelete(IndexEvent event, KafkaMessageEntity entity)throws Exception{
        List<String> primaryKeys = entity.getPrimaryKeys();
        if(null == primaryKeys || primaryKeys.size() == 0){
            throw new Exception("Table " + entity.getSchemaName() +"." + entity.getTableName() + "must have primary key");
        }
        String primaryKey = primaryKeys.get(0);
        JSONObject oldData = entity.getOldRow();
        Map<String, Object> condition = new HashMap<>();
        condition.put(primaryKey, oldData.get(primaryKey));
        event.setCondition(condition);
    }
}
