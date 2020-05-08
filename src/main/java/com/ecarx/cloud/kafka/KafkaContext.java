package com.ecarx.cloud.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class KafkaContext {
    private static KafkaContext context;
    private Properties configs = new Properties();
    private List<String> topics = new ArrayList<>();
    private int cacheCapacity;

    private KafkaContext(){}

    public static KafkaContext getInstance(){
        if(null == context){
            synchronized (KafkaContext.class){
                if(null == context){
                    context = new KafkaContext();
                }
            }
        }
        return context;
    }

    public void addConfig(String key, Object value){
        configs.put(key, value);
    }

    public List<String> getTopics() {
        return topics;
    }

    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    public Properties getConfigs() {
        return configs;
    }

    public int getCacheCapacity() {
        return cacheCapacity;
    }

    public void setCacheCapacity(int cacheCapacity) {
        this.cacheCapacity = cacheCapacity;
    }
}
