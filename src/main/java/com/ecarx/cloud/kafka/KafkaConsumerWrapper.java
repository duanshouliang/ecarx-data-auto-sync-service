package com.ecarx.cloud.kafka;

import com.alibaba.fastjson.JSON;
import com.ecarx.cloud.cache.CacheEntity;
import com.ecarx.cloud.cache.LexicalItemCache;
import com.ecarx.cloud.task.TaskDispatcher;
import com.ecarx.cloud.task.IndexTaskRunner;
import com.ecarx.cloud.monitor.LexiconUpdatedMonitor;
import com.ecarx.cloud.monitor.LexiconUpdatingMonitor;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class KafkaConsumerWrapper implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerWrapper.class);

    private static final String GROUP_ID_KEY="group.id";
    private KafkaContext context;
    private KafkaConsumer<String, String> consumer;
    private IndexTaskRunner indexTaskRunner;
    private TransportClient transportClient;
    private LexicalItemCache lexicalItemCache;
    private LexiconUpdatingMonitor lexiconUpdatingMonitor;
    private LexiconUpdatedMonitor lexiconUpdatedMonitor;
    private TaskDispatcher taskDispatcher;

    public KafkaConsumerWrapper(KafkaContext context, IndexTaskRunner indexTaskRunner, TransportClient transportClient){
        this.context = context;
        this.indexTaskRunner = indexTaskRunner;
        this.transportClient = transportClient;
        lexicalItemCache = new LexicalItemCache(context.getCacheCapacity());
        lexiconUpdatingMonitor = new LexiconUpdatingMonitor();
        lexiconUpdatedMonitor = new LexiconUpdatedMonitor();
        lexiconUpdatedMonitor.setTransportClient(transportClient);

        taskDispatcher = new TaskDispatcher();
        taskDispatcher.setIndexTaskRunner(indexTaskRunner)
                .setLexicalItemCache(lexicalItemCache)
                .setLexiconUpdatingMonitor(lexiconUpdatingMonitor)
                .setLexiconUpdatedMonitor(lexiconUpdatedMonitor);
    }

    public void subscribe(String topic){
        List<String> topics = new ArrayList<>();
        topics.addAll(Arrays.asList(topic.split(";")));
        Properties props = context.getConfigs();
        props.put(GROUP_ID_KEY, topic + "_consumer");
        consumer = new KafkaConsumer<>(props);
        ConsumerRebalanceListener listener = new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                LOGGER.info(String.format("Kafka rebalance revoked: %s for topic %s", JSON.toJSONString(partitions), topic));
            }
            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                LOGGER.info(String.format("Kafka consumer assigned response %s for topic %s", JSON.toJSONString(partitions),topic));
            }
        };
        consumer.subscribe(topics, listener);
    }
    @Override
    public void run() {
        while (!Thread.interrupted()) {
            //若缓存大小不够则暂停消费
            if(lexicalItemCache.getLeftCapacity() < 1 || lexiconUpdatingMonitor.isUpdateLexicon()){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                LOGGER.info("Waiting dictionary update complete!");
            }
            for(int i=0;i<100;i++){
                lexicalItemCache.put(new CacheEntity());
            }
            ConsumerRecords<String, String> records = consumer.poll(100);
            records.forEach(record ->{
                CacheEntity cacheEntity = KafkaRecordParser.parser(record, transportClient);
                if(null != cacheEntity){
                    lexicalItemCache.put(cacheEntity);
                }
            });
        }
    }
}
