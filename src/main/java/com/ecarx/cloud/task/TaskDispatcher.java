package com.ecarx.cloud.task;

import com.ecarx.cloud.cache.CacheEntity;
import com.ecarx.cloud.cache.LexicalItemCache;
import com.ecarx.cloud.dict.Dictionary;
import com.ecarx.cloud.kafka.KafkaRecordParser;
import com.ecarx.cloud.monitor.LexiconUpdatedMonitor;
import com.ecarx.cloud.monitor.LexiconUpdatingMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TaskDispatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDispatcher.class);

    private Thread worker;
    private LexicalItemCache lexicalItemCache;
    private LexiconUpdatingMonitor lexiconUpdatingMonitor;
    private LexiconUpdatedMonitor lexiconUpdatedMonitor;
    private IndexTaskRunner indexTaskRunner;

    public TaskDispatcher(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    List<IndexTask> indexTasks = null;
                    if(lexicalItemCache.getLeftCapacity() < 1 || lexiconUpdatingMonitor.isUpdateLexicon()){
                        List<CacheEntity> cacheEntityList = lexicalItemCache.pull();
                        Map<String, Set<String>> wordsToUpdate = new HashMap<>();
                        Set<String> lexicalItems = new HashSet<>();
                        indexTasks = new ArrayList<>();
                        //合并缓存，不同的cp需要更新不同的词库
                        KafkaRecordParser.merge(cacheEntityList, wordsToUpdate, lexicalItems, indexTasks);
                        //wordsToUpdate提交词库更新任务
                        //词库更新器,根据实际情况更新具体的词库

                        lexiconUpdatedMonitor.setLexicalItems(lexicalItems);
                    }
                    if(lexiconUpdatedMonitor.isLexiconUpdated()){
                        submitTask(indexTasks);
                        lexiconUpdatingMonitor.reset();
                        lexiconUpdatedMonitor.reset();
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        worker.start();
    }

    public void submitTask(List<IndexTask> tasks){
        if(null != tasks && tasks.size() != 0){
            tasks.forEach(task ->{
                indexTaskRunner.submitTask(task);
            });
        }
    }

    public TaskDispatcher setLexicalItemCache(LexicalItemCache lexicalItemCache) {
        this.lexicalItemCache = lexicalItemCache;
        return this;
    }

    public TaskDispatcher setLexiconUpdatingMonitor(LexiconUpdatingMonitor lexiconUpdatingMonitor) {
        this.lexiconUpdatingMonitor = lexiconUpdatingMonitor;
        return this;
    }

    public TaskDispatcher setLexiconUpdatedMonitor(LexiconUpdatedMonitor lexiconUpdatedMonitor) {
        this.lexiconUpdatedMonitor = lexiconUpdatedMonitor;
        return this;
    }

    public TaskDispatcher setIndexTaskRunner(IndexTaskRunner indexTaskRunner) {
        this.indexTaskRunner = indexTaskRunner;
        return this;
    }
}
