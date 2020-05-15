package com.ecarx.cloud.kafka;

import com.alibaba.fastjson.JSONObject;
import com.ecarx.cloud.business.music.factory.EventHandlerSelector;
import com.ecarx.cloud.business.music.factory.IndexerSelector;
import com.ecarx.cloud.constant.AnalysisFieldEnum;
import com.ecarx.cloud.constant.IndexEventEnum;
import com.ecarx.cloud.dict.Dictionary;
import com.ecarx.cloud.dict.LexicalItemUtil;
import com.ecarx.cloud.dict.cache.CacheEntity;
import com.ecarx.cloud.elasticsearch.index.IndexEvent;
import com.ecarx.cloud.elasticsearch.index.Indexer;
import com.ecarx.cloud.task.IndexTask;
import com.ecarx.cloud.util.ChineseDetectUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KafkaRecordParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRecordParser.class);

    public static CacheEntity parser(ConsumerRecord<String, String> record, TransportClient client) {
        String rowContent = record.value();
        if (StringUtils.isBlank(rowContent)) {
            return null;
        }
        KafkaMessageEntity messageEntity = null;
        try {
            messageEntity = JSONObject.parseObject(rowContent, KafkaMessageEntity.class);
        } catch (Exception e) {
            LOGGER.error("parser kafka message with exception {}", e.getMessage());
        }

        if(null == messageEntity){
            return null;
        }
        String eventType = messageEntity.getEventType();
        String business = messageEntity.getSchemaName() + "." + messageEntity.getTableName();
        Integer kind = AnalysisFieldEnum.getKind(business);
        if (null == kind) {
            kind = 0;
        }
        CacheEntity cacheEntity = new CacheEntity();
        cacheEntity.setKind(kind);

        Indexer indexer = IndexerSelector.select(business, client);
        IndexEvent indexEvent = EventHandlerSelector.selector(business).handle(messageEntity);
        if (null != indexer && null != indexEvent) {
            cacheEntity.setTask(new IndexTask(indexEvent, indexer));
        }
        String word = null;
        if (eventType.equals(IndexEventEnum.ADD.getValue())) {
            JSONObject newRow = messageEntity.getNewRow();
            if (newRow.containsKey(AnalysisFieldEnum.getField(business))) {
                word = newRow.getString(AnalysisFieldEnum.getField(business));
            }
        } else if (eventType.equals(IndexEventEnum.UPDATE.getValue())) {
            JSONObject newRow = messageEntity.getNewRow();
            JSONObject oldRow = messageEntity.getOldRow();
            String newWord = newRow.getString(AnalysisFieldEnum.getField(business));
            String oldWord = oldRow.getString(AnalysisFieldEnum.getField(business));
            if (StringUtils.isNotBlank(newWord) && (StringUtils.isBlank(oldWord) || !newWord.equals(oldWord))) {
                word = newWord;
            } else {
                cacheEntity.setDirectTask(true);
            }
        } else if (eventType.equals(IndexEventEnum.DELETE.getValue())) {
            cacheEntity.setDirectTask(true);
            return cacheEntity;
        }
        if (StringUtils.isNotBlank(word)) {
            Set<String> words = LexicalItemUtil.getLexicalItems(word, kind);
            if (null != words && words.size() != 0) {
                cacheEntity.setWords(words);
            }
        }
        return cacheEntity;
    }

    public static void taskDispatch(List<CacheEntity> cacheEntityList, Map<Integer, Set<String>> cpWords, Set<String> lexicalItems, List<IndexTask> indexTasks) {
        for (CacheEntity cacheEntity : cacheEntityList) {
            if (!cacheEntity.isDirectTask()) {
                indexTasks.add(cacheEntity.getTask());
            }

            Integer kind = cacheEntity.getKind();
            Set<String> words = cacheEntity.getWords();
            if (null == words || words.size() == 0) {
                continue;
            }
            if (cpWords.containsKey(kind)) {
                cpWords.get(kind).addAll(words);
            } else {
                cpWords.put(kind, words);
            }
        }
        for (Map.Entry<Integer, Set<String>> entry : cpWords.entrySet()) {
            Set<String> words = Dictionary.filter(entry.getKey(), entry.getValue());
            entry.setValue(words);
            for(String word : words){
                if(ChineseDetectUtil.isFullChinese(word)){
                    lexicalItems.add(word);
                    break;
                }
            }
        }
    }
}
