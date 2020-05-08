package com.ecarx.cloud.elasticsearch.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ecarx.cloud.elasticsearch.event.IndexEvent;
import com.ecarx.cloud.elasticsearch.handler.indexer.selector.EventHandlerSelector;
import com.ecarx.cloud.elasticsearch.index.indexer.Indexer;
import com.ecarx.cloud.elasticsearch.index.indexer.selector.IndexerSelector;
import com.ecarx.cloud.kafka.KafkaMessageEntity;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.client.transport.TransportClient;

public class IndexerTaskBuilder {
    public static IndexerTask build(ConsumerRecord<String, String> record, TransportClient client){
        String data = record.value();
        KafkaMessageEntity entity = JSONObject.parseObject(data, KafkaMessageEntity.class);
        String business = entity.getSchemaName() +"_"+ entity.getTableName();
        Indexer indexer = IndexerSelector.select(business, client);
        IndexEvent event = EventHandlerSelector.selector(business).handle(entity);
        if(null != event && null != indexer) {
            return new IndexerTask(event, indexer);
        }else{
            return null;
        }
    }
}
