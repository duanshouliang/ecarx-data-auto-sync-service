package com.ecarx.cloud.kafka;

import com.alibaba.fastjson.JSONObject;
import com.ecarx.cloud.cache.CacheEntity;
import com.ecarx.cloud.elasticsearch.event.IndexEvent;
import com.ecarx.cloud.elasticsearch.handler.indexer.selector.EventHandlerSelector;
import com.ecarx.cloud.elasticsearch.index.indexer.Indexer;
import com.ecarx.cloud.elasticsearch.index.indexer.selector.IndexerSelector;
import com.ecarx.cloud.task.IndexTask;
import com.ecarx.cloud.enumeration.IndexEventEnum;
import com.ecarx.cloud.enumeration.AnalysisFieldEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.client.transport.TransportClient;

import java.util.*;

public class KafkaRecordParser {

    public static CacheEntity parser( ConsumerRecord<String, String> record, TransportClient client){
        String rowContent = record.value();
        if(StringUtils.isBlank(rowContent)){
            return null;
        }
        KafkaMessageEntity messageEntity = JSONObject.parseObject(rowContent, KafkaMessageEntity.class);

        String eventType = messageEntity.getEventType();
        if(eventType.equals(IndexEventEnum.DELETE.getValue())){
            return null;
        }
        CacheEntity cacheEntity = new CacheEntity();
        String business = messageEntity.getSchemaName() +"."+ messageEntity.getTableName();
        cacheEntity.setKind(AnalysisFieldEnum.getKind(business));
        cacheEntity.setTasks(new ArrayList<>());
        cacheEntity.setWords(new ArrayList<>());

        Indexer indexer = IndexerSelector.select(business, client);
        IndexEvent indexEvent = EventHandlerSelector.selector(business).handle(messageEntity);
        if(null != indexer && null != indexEvent){
            cacheEntity.getTasks().add(new IndexTask(indexEvent, indexer));
        }

        String word = null;
        if(eventType.equals(IndexEventEnum.ADD.getValue()) ){
            JSONObject newRow = messageEntity.getNewRow();
            if(newRow.containsKey(AnalysisFieldEnum.getField(business)) || eventType.equals(IndexEventEnum.UPDATE.getValue())){
               word = newRow.getString(AnalysisFieldEnum.getField(business));
            }
        }
        if(StringUtils.isNotBlank(word)){
            cacheEntity.getWords().add(word);
        }
        return cacheEntity;
    }

    public static void merge(List<CacheEntity> cacheEntityList, Map<Integer, Set<String>> wordsToUpdate, Set<String> lexicalItems, List<IndexTask> indexTasks){

        for(CacheEntity cacheEntity : cacheEntityList){
            Integer kind = cacheEntity.getKind();
            List<String> words = cacheEntity.getWords();
            indexTasks.addAll(cacheEntity.getTasks());
            if(wordsToUpdate.containsKey(kind)){
                wordsToUpdate.get(kind).addAll(words);
            }else{
                Set<String> cpWords = new HashSet<>();
                cpWords.addAll(words);
                wordsToUpdate.put(kind, cpWords);
            }
        }

        for(Map.Entry<Integer, Set<String>> entry : wordsToUpdate.entrySet()){
            List<String> tmp = new ArrayList<>(entry.getValue());
            lexicalItems.add(tmp.get(0));
            tmp = null;
        }
    }
}
