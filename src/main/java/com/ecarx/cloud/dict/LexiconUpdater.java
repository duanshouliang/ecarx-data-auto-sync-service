package com.ecarx.cloud.dict;

import com.ecarx.cloud.cache.CacheEntity;
import com.ecarx.cloud.cache.LexicalItemCache;
import com.ecarx.cloud.elasticsearch.index.runner.IndexRunner;
import com.ecarx.cloud.elasticsearch.task.IndexerTask;
import com.ecarx.cloud.monitor.DataSyncMonitor;
import com.ecarx.cloud.monitor.LexiconUpdatedMonitor;
import com.ecarx.cloud.monitor.LexiconUpdatingMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LexiconUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger(LexiconUpdater.class);

    private Thread worker;
    private LexicalItemCache lexicalItemCache;
    private LexiconUpdatingMonitor lexiconUpdatingMonitor;
    private LexiconUpdatedMonitor lexiconUpdatedMonitor;
    private IndexRunner indexRunner;
    private List<IndexerTask> tasks;

    public LexiconUpdater(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(lexiconUpdatedMonitor.isLexiconUpdated()){
                        submitTask(tasks);
                        lexiconUpdatingMonitor.reset();
                        lexiconUpdatedMonitor.reset();
                    }
                    if(lexicalItemCache.getLeftCapacity() < 1 || lexiconUpdatingMonitor.isUpdateLexicon()){
                        List<CacheEntity> cacheEntityList = lexicalItemCache.pull();
                        tasks = new ArrayList<>();
                        Set<String> lexicalItems = new HashSet<>();
                        for(CacheEntity entity : cacheEntityList){
                            List<String> words = entity.getWords();
                            if(null != words && words.size()!=0){
                                lexicalItems.add(words.get(words.size()-1));
                            }
                        }
                        lexiconUpdatedMonitor.setLexicalItems(lexicalItems);

                    }
                }
            }
        });
        worker.start();
    }

    public void submitTask(List<IndexerTask> tasks){
        if(null != tasks && tasks.size() != 0){
            tasks.forEach(task ->{
                indexRunner.submitTask(task);
            });
        }
    }

    public LexiconUpdater setLexicalItemCache(LexicalItemCache lexicalItemCache) {
        this.lexicalItemCache = lexicalItemCache;
        return this;
    }

    public LexiconUpdater setLexiconUpdatingMonitor(LexiconUpdatingMonitor lexiconUpdatingMonitor) {
        this.lexiconUpdatingMonitor = lexiconUpdatingMonitor;
        return this;
    }

    public LexiconUpdater setLexiconUpdatedMonitor(LexiconUpdatedMonitor lexiconUpdatedMonitor) {
        this.lexiconUpdatedMonitor = lexiconUpdatedMonitor;
        return this;
    }

    public LexiconUpdater setIndexRunner(IndexRunner indexRunner) {
        this.indexRunner = indexRunner;
        return this;
    }
}
