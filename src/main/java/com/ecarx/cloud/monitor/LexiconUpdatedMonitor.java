package com.ecarx.cloud.monitor;

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
public class LexiconUpdatedMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LexiconUpdatedMonitor.class);
    private Set<String> lexicalItems;
    private Thread worker;
    private TransportClient transportClient;
    private boolean lexiconUpdated;

    public LexiconUpdatedMonitor(){
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()){
                    if(!lexiconUpdated && null != lexicalItems && lexicalItems.size() != 0){
                        for(String lexicalItem : lexicalItems){
                            lexiconUpdated = isLexiconUpdateFinish(lexicalItem);
                            if(!lexiconUpdated){
                                break;
                            }
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
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

    public LexiconUpdatedMonitor setTransportClient(TransportClient transportClient) {
        this.transportClient = transportClient;
        return this;
    }

    public void reset(){
        this.lexiconUpdated = false;
        this.lexicalItems = null;
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
}
