package com.ecarx.cloud.task;

import com.ecarx.cloud.dict.Dictionary;
import com.ecarx.cloud.dict.cache.CacheEntity;
import com.ecarx.cloud.dict.cache.DictCache;
import com.ecarx.cloud.dict.monitor.DictUpdatedMonitor;
import com.ecarx.cloud.dict.monitor.DictUpdatingMonitor;
import com.ecarx.cloud.kafka.KafkaRecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TaskDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDispatcher.class);

    private Thread worker;
    private DictCache dictCache;
    private DictUpdatingMonitor dictUpdatingMonitor;
    private DictUpdatedMonitor dictUpdatedMonitor;

    public TaskDispatcher(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    if(dictCache.getLeftCapacity() < 1 || dictUpdatingMonitor.isUpdatingDict()){
                        List<CacheEntity> cacheEntityList = dictCache.pull();
                        if(null == cacheEntityList || cacheEntityList.size() == 0){
                            dictUpdatingMonitor.reset();
                            continue;
                        }
                        //不同数据源对应的词项
                        Map<Integer, Set<String>> cpWords = new HashMap<>();
                        //每个数据中获取一个词项，用于判断词库是否更新完成
                        Set<String> lexicalItems = new HashSet<>();
                        //数据同步任务
                        List<IndexTask>  indexTasks = new ArrayList<>();
                        KafkaRecordParser.taskDispatch(cacheEntityList, cpWords, lexicalItems, indexTasks);

                        dictUpdatedMonitor.setTasks(indexTasks);
                        Dictionary.getInstance().addWordTasks(cpWords);
                        dictUpdatedMonitor.setLexicalItems(lexicalItems);
                    }
                    if(dictUpdatedMonitor.isDictUpdated()){
                        dictUpdatingMonitor.reset();
                        dictUpdatedMonitor.reset();
                        LOGGER.info("Dictionary has been updated!");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                LOGGER.info("Task dispatcher Thread has stop");
            }
        });
        worker.start();
    }

    public TaskDispatcher setDictCache(DictCache dictCache) {
        this.dictCache = dictCache;
        return this;
    }

    public TaskDispatcher setDictUpdatingMonitor(DictUpdatingMonitor dictUpdatingMonitor) {
        this.dictUpdatingMonitor = dictUpdatingMonitor;
        return this;
    }

    public TaskDispatcher setDictUpdatedMonitor(DictUpdatedMonitor dictUpdatedMonitor) {
        this.dictUpdatedMonitor = dictUpdatedMonitor;
        return this;
    }
}
