package com.ecarx.cloud.dict.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class DictCache {
    private ArrayBlockingQueue<CacheEntity> queue;
    private int left;
    public DictCache(int capacity){
        this.left = capacity;
        queue = new ArrayBlockingQueue<>(capacity);
    }

    public void put(CacheEntity cacheEntity){
        try {
            queue.put(cacheEntity);
            this.left --;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public List<CacheEntity> pull(){
        List<CacheEntity> cacheEntities = null;
        if(queue.size() != 0){
            cacheEntities = new ArrayList<>();
            this.left += queue.size();
            queue.drainTo(cacheEntities, queue.size());
        }
        return cacheEntities;
    }

    public int getLeftCapacity(){
        return this.left;
    }
}
