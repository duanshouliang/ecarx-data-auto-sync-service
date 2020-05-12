package com.ecarx.cloud.kafka;

import com.alibaba.fastjson.JSONObject;
import com.ecarx.cloud.dict.cache.CacheEntity;
import com.ecarx.cloud.elasticsearch.event.IndexEvent;
import com.ecarx.cloud.elasticsearch.handler.indexer.selector.EventHandlerSelector;
import com.ecarx.cloud.elasticsearch.index.indexer.Indexer;
import com.ecarx.cloud.elasticsearch.index.indexer.selector.IndexerSelector;
import com.ecarx.cloud.enumeration.AnalysisFieldEnum;
import com.ecarx.cloud.enumeration.IndexEventEnum;
import com.ecarx.cloud.task.IndexTask;
import com.ecarx.cloud.util.ChineseDetectUtil;
import com.ecarx.cloud.util.CommonUtil;
import com.ecarx.cloud.util.ZhConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.client.transport.TransportClient;

import java.util.*;

public class KafkaRecordParser {

    public static CacheEntity parser(ConsumerRecord<String, String> record, TransportClient client) {
        String rowContent = record.value();
        if (StringUtils.isBlank(rowContent)) {
            return null;
        }
        KafkaMessageEntity messageEntity = JSONObject.parseObject(rowContent, KafkaMessageEntity.class);

        String eventType = messageEntity.getEventType();
        String business = messageEntity.getSchemaName() + "." + messageEntity.getTableName();
        Integer kind = AnalysisFieldEnum.getKind(business);
        if (null == kind) {
            return null;
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
            Set<String> words = handleWord(word, kind);
            if (null != words && words.size() != 0) {
                cacheEntity.setWords(words);
            }
        }
        return cacheEntity;
    }

    private static Set<String> handleWord(String word, Integer kind) {
        word = word.trim();
        if (!ChineseDetectUtil.containChinese(word)) {
            return null;
        }

        Set<String> words = new HashSet<>();
        String wordWithoutSpecialCharacter = CommonUtil.removeSpecialCharacter(word);
        if (StringUtils.isNotBlank(wordWithoutSpecialCharacter)) {
            List<String> list = CommonUtil.splitByBlank(wordWithoutSpecialCharacter);
            list.forEach(item -> {
                if (StringUtils.isNotBlank(item)) {
                    words.add(item.trim());
                }
            });
        }

        if (kind == AnalysisFieldEnum.ALBUM.getKind()) {
            word = CommonUtil.removeParenthesisAndContent(word);
        } else if (kind == AnalysisFieldEnum.ARTIST.getKind()) {
            word = CommonUtil.removeBraketAndContent(word);
        }
        if (StringUtils.isNotBlank(word.trim())) {
            words.add(ZhConverter.convert(word, ZhConverter.SIMPLIFIED));
        }
        return words;
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
            List<String> tmp = new ArrayList<>(entry.getValue());
            lexicalItems.add(tmp.get(0));
            tmp = null;
        }
    }
}
