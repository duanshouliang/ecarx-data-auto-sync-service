package com.ecarx.cloud.elasticsearch.index.runner;

import com.alibaba.fastjson.JSON;
import com.ecarx.cloud.common.Result;
import com.ecarx.cloud.elasticsearch.event.IndexEvent;
import com.ecarx.cloud.task.IndexTaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DataHandleRequestRunner{
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexTaskRunner.class);
    private BlockingQueue<IndexEvent> events;
    private Thread worker;

    public DataHandleRequestRunner(String topic){
        events = new LinkedBlockingQueue<>(200);
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                int i=0;
                while (!Thread.interrupted()){
                    LOGGER.info((i++)+"");
                    IndexEvent task = null;
                    try {
                        task = events.take();
                        LOGGER.info("contnet: "+ JSON.toJSONString(task));
                    } catch (InterruptedException e) {
                        LOGGER.error("Get new task with exception {}, stack {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
                    }
                    Result<String> result = null;
                    try {
                        Map<String, Object> data = task.getFields();
                    }catch (Exception e){
                        LOGGER.error("Sync data to elasticsearch with exception {}", e.getMessage());
                    }
                    if(null != result && !result.isSuccess()){
                        LOGGER.error("Sync data {} to elasticsearch with failed for the reason that {}", JSON.toJSONString(task), result.getMsg());
                    }
                }
            }
        });
        worker.setName(topic);
        worker.start();
    }

    public void submitTask(IndexEvent task){
        events.add(task);
    }
}
