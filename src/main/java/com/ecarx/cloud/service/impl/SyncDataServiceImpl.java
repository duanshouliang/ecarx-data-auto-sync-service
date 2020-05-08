package com.ecarx.cloud.service.impl;

import com.ecarx.cloud.elasticsearch.index.runner.IndexRunner;
import com.ecarx.cloud.kafka.KafkaConsumerWrapper;
import com.ecarx.cloud.kafka.KafkaContext;
import com.ecarx.cloud.service.SyncDataService;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Service
public class SyncDataServiceImpl implements SyncDataService {

    @Autowired
    private KafkaContext context;

    @Autowired
    private TransportClient transportClient;

    private ExecutorService executors;


    @PostConstruct
    public void init(){
        List<String> topics = context.getTopics();
        //创建线程池
        ThreadFactory threadFactory = new DefaultThreadFactory("Sync data execute") {
            @Override
            protected Thread newThread(Runnable r, String name) {
                return new Thread(r, name);
            }
        };
        //创建线程执行器
        executors = Executors.newCachedThreadPool(threadFactory);
        topics.forEach(topic ->{
            IndexRunner indexRunner = new IndexRunner(topic +" elasticsearch thread");
            KafkaConsumerWrapper consumer = new KafkaConsumerWrapper(context, indexRunner,transportClient);
            consumer.subscribe(topic);
            executors.submit(consumer);
        });
    }
}
