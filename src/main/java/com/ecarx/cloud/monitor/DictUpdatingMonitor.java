package com.ecarx.cloud.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监控是否开始更新词库
 *
 */
public class DictUpdatingMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictUpdatingMonitor.class);

    private AtomicInteger counter = new AtomicInteger(50);

    /**
     * 是否开始同步词库
     */
    private boolean updatingLexicon = false;

    private Thread worker;

    public DictUpdatingMonitor(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!updatingLexicon){
                        int current = counter.decrementAndGet();
                        if(current == 0){
                            updatingLexicon = true;
                        }
                    }
                }
                LOGGER.info("Lexicon updating monitor Thread has stop");
            }
        });
        worker.start();
    }

    public boolean isUpdatingLexicon(){
        return updatingLexicon;
    }

    public void reset(){
        this.counter.set(50);
        this.updatingLexicon = false;
    }
}
