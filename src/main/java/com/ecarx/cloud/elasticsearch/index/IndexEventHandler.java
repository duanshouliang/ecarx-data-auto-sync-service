package com.ecarx.cloud.elasticsearch.index;


import com.ecarx.cloud.kafka.KafkaMessageEntity;

/**
 *
 * 将从kafka中接受到的消息处理成elasticsearchdocument
 *
 */
public class IndexEventHandler {
    public IndexEvent handle(KafkaMessageEntity entity){
        return new IndexEvent();
    }
}
