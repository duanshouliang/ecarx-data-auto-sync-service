package com.ecarx.cloud.monitor;

import com.ecarx.cloud.elasticsearch.index.runner.IndexRunner;
import com.ecarx.cloud.elasticsearch.task.IndexerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * 监控是否开始同步数据到elasticsearch中
 */
public class DataSyncMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSyncMonitor.class);

    private Thread worker;
    private IndexRunner indexRunner;
    private boolean start;

    public DataSyncMonitor(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        worker.start();
    }

    public void setStart(boolean start) {
        this.start = start;
    }

    public DataSyncMonitor setIndexRunner(IndexRunner indexRunner) {
        this.indexRunner = indexRunner;
        return this;
    }

    public void submitTask(List<IndexerTask> tasks){
        if(start){
            if(null != tasks && tasks.size() != 0){
                tasks.forEach(task ->{
                    indexRunner.submitTask(task);
                });
            }
        }
    }

    public void reset(){
        this.start = false;
    }
}
