package com.ecarx.cloud.config;

import com.ecarx.cloud.kafka.KafkaContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.servers}")
    private String servers;
    @Value("${spring.kafka.group_id}")
    private String groupId;
    @Value("${spring.kafka.auto_commit_interval}")
    private long autoCommitInterval;
    @Value("${spring.kafka.max_partition_fetch_bytes}")
    private int maxPartitionFetchBytes;
    @Value("${spring.kafka.session_timeout}")
    private long sessionTimeout;
    @Value("${spring.kafka.max_poll_interval}")
    private long maxPollInterval;
    @Value("${spring.kafka.auto_offset_reset}")
    private String autoOffsetReset;
    @Value("${spring.kafka.max_poll_records}")
    private int maxPollRecords;
    @Value("${spring.kafka.enable_auto_commit}")
    private boolean enableAutoCommit;
    @Value("${spring.kafka.topics}")
    private String topics;
    @Value("${spring.dict.cache.capacity}")
    private int cacheCapacity;


    @Bean
    public KafkaContext getKafkaContext(){
        KafkaContext context = KafkaContext.getInstance();
        context.addConfig("bootstrap.servers", servers);
        /* 是否自动确认offset */
        context.addConfig("enable.auto.commit", enableAutoCommit);
        context.addConfig("max.partition.fetch.bytes", maxPartitionFetchBytes);
//        context.addConfig("max.poll.records", 1000);
//        context.addConfig("group.id", groupId);
        /* 自动确认offset的时间间隔 */
        context.addConfig("auto.commit.interval.ms", autoCommitInterval);
        context.addConfig("session.timeout.ms", 30000);
        context.addConfig("max.poll.interval.ms", maxPollInterval);
        context.addConfig("auto.offset.reset", autoOffsetReset); // 必须加
        /* key的序列化类 */
        context.addConfig("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        /* value的序列化类 */
        context.addConfig("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        if(cacheCapacity < 1){
            cacheCapacity = 5000;
        }
        context.setCacheCapacity(cacheCapacity);
        if(StringUtils.isNoneBlank(topics)){
            context.setTopics(Arrays.asList(StringUtils.split(topics, ";")));
        }
        return context;
    }
}
