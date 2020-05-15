package com.ecarx.cloud.task;

import com.alibaba.fastjson.JSON;
import com.ecarx.cloud.common.Result;
import com.ecarx.cloud.elasticsearch.index.IndexEvent;
import com.ecarx.cloud.elasticsearch.index.Indexer;
import com.ecarx.cloud.constant.IndexEventEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IndexTaskRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexTaskRunner.class);
    private BlockingQueue<IndexTask> tasks;
    private Thread worker;

    public IndexTaskRunner(String topic){
        tasks = new LinkedBlockingQueue<>(200);
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                int i=0;
                while (!Thread.interrupted()){
                    LOGGER.info((i++)+"");
                    IndexTask task = null;
                    try {
                        task = tasks.take();
                        LOGGER.info("contnet: "+JSON.toJSONString(task));
                    } catch (InterruptedException e) {
                        LOGGER.error("Get new task with exception {}, stack {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                    }
                    Result<String> result = null;
                    IndexEvent event = task.getEvent();
                    Indexer indexer = task.getIndexer();
                    try {
                        String operate = event.getCmd();
                        if(operate.equalsIgnoreCase(IndexEventEnum.ADD.getValue())){
                            result = indexer.add(event);
                        }else if(operate.equalsIgnoreCase(IndexEventEnum.UPDATE.getValue())){
                            result = indexer.update(event);
                        }else if(operate.equalsIgnoreCase(IndexEventEnum.DELETE.getValue())){
                            result = indexer.delete(event);
                        }else{
                            result = Result.failed("Illegal operation " + operate);
                            throw new Exception("Illegal operation " + operate);
                        }
                    }catch (Exception e){
                        LOGGER.error("Sync data to elasticsearch with exception {}", e.getMessage());
                    }
                    if(null != result && !result.isSuccess()){
                        LOGGER.error("Sync data {} to elasticsearch with failed for the reason that {}", JSON.toJSONString(event), result.getMsg());
                    }
                }
            }
        });
        worker.setName(topic);
        worker.start();
    }
    public void submitTask(IndexTask task){
        tasks.add(task);
    }
}
