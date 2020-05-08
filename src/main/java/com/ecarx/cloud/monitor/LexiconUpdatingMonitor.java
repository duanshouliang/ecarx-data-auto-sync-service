package com.ecarx.cloud.monitor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监控是否开始更新词库
 *
 */
public class LexiconUpdatingMonitor {
    private AtomicInteger counter = new AtomicInteger(50);

    /**
     * 是否开始同步词库
     */
    private boolean updateLexicon = false;

    private Thread worker;

    public LexiconUpdatingMonitor(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(!updateLexicon){
                        int current = counter.decrementAndGet();
                        if(current == 0){
                            updateLexicon = true;
                        }
                    }
                }
            }
        });
        worker.start();
    }

    public boolean isUpdateLexicon(){
        return updateLexicon;
    }

    public void reset(){
        this.counter.set(50);
        this.updateLexicon = false;
    }
}
