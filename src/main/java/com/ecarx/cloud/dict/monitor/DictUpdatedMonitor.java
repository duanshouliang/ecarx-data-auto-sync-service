package com.ecarx.cloud.dict.monitor;

import com.ecarx.cloud.task.IndexTask;
import com.ecarx.cloud.task.IndexTaskRunner;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监控词库是否更新完成
 *
 */
public class DictUpdatedMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictUpdatedMonitor.class);
    private Set<String> lexicalItems;
    private Thread worker;
    private TransportClient transportClient;
    private IndexTaskRunner indexTaskRunner;
    private boolean dictUpdated;
    private List<IndexTask> tasks;
    private boolean checking;

    public DictUpdatedMonitor(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    if(checking && !dictUpdated){
                        if(null == lexicalItems || lexicalItems.size() == 0){
                            //submitTask(tasks);
                            checking = false;
                            LOGGER.info("Diction has updated for words, start submit sync data task");
                        }else {
                            for (String lexicalItem : lexicalItems) {
                                boolean result = isDictUpdated(lexicalItem);
                                if (!result) {
                                    continue;
                                } else {
                                    //词库更新完成，则提交同步数据的任务,待开发
                                    checking = false;
                                    dictUpdated = true;
                                    //submitTask(tasks);
                                    LOGGER.info("Diction has updated for words, start submit sync data task");
                                }
                            }
                        }
                    }else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                LOGGER.info("Dictionary updated monitor Thread has stop");
            }
        });
        worker.start();
    }

    private boolean isDictUpdated(String lexicalItem){
        boolean result = false;
        AnalyzeRequest analyzeRequest = new AnalyzeRequest()
                .text(lexicalItem)
                .analyzer("ik_smart");
        try {
            List<AnalyzeResponse.AnalyzeToken> tokens = transportClient.admin().indices()
                    .analyze(analyzeRequest)
                    .actionGet()
                    .getTokens();

            for(AnalyzeResponse.AnalyzeToken token : tokens){
                if(lexicalItem.equals(token.getTerm())){
                    result = true;
                    break;
                }
            }
        }catch (Exception e){
            LOGGER.error("Judge dictionary update finish with exception {}", e.getMessage());
        }
        return result;
    }

    public void submitTask(List<IndexTask> tasks){
        if(null != tasks && tasks.size() != 0){
            tasks.forEach(task ->{
                indexTaskRunner.submitTask(task);
            });
        }
    }

    public DictUpdatedMonitor setTransportClient(TransportClient transportClient) {
        this.transportClient = transportClient;
        return this;
    }

    public DictUpdatedMonitor setIndexTaskRunner(IndexTaskRunner indexTaskRunner) {
        this.indexTaskRunner = indexTaskRunner;
        return this;
    }

    public void reset(){
        this.dictUpdated = false;
        this.lexicalItems = null;
        this.tasks = null;
        this.checking = false;
    }

    public boolean isDictUpdated() {
        return dictUpdated;
    }

    public void setDictUpdated(boolean dictUpdated) {
        this.dictUpdated = dictUpdated;
    }

    public Set<String> getLexicalItems() {
        return lexicalItems;
    }

    public void setLexicalItems(Set<String> lexicalItems) {
        this.lexicalItems = lexicalItems;
    }

    public List<IndexTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<IndexTask> tasks) {
        this.tasks = tasks;
    }

    public boolean isChecking() {
        return checking;
    }

    public void setChecking(boolean checking) {
        this.checking = checking;
    }
}
