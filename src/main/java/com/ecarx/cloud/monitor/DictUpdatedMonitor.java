package com.ecarx.cloud.monitor;

import com.ecarx.cloud.task.IndexTask;
import com.ecarx.cloud.task.IndexTaskRunner;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

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
    private boolean lexiconUpdated;
    private List<IndexTask> tasks;

    public DictUpdatedMonitor(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    if(!lexiconUpdated && null != lexicalItems && lexicalItems.size() != 0){
                        for(String lexicalItem : lexicalItems){
                            lexiconUpdated = isLexiconUpdateFinish(lexicalItem);
                            if(!lexiconUpdated){
                               continue;
                            }else{
                                //词库更新完成，则提交同步数据的任务
                                submitTask(tasks);
                            }
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                LOGGER.info("Lexicon updated monitor Thread has stop");
            }
        });
        worker.start();
    }

    private boolean isLexiconUpdateFinish(String lexicalItem){
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
                    result = false;
                    break;
                }
            }
        }catch (Exception e){
            LOGGER.error("Judge lexicon update finish with exception {}", e.getMessage());
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
        this.lexiconUpdated = false;
        this.lexicalItems = null;
        this.tasks = null;
    }

    public boolean isLexiconUpdated() {
        return lexiconUpdated;
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
}
