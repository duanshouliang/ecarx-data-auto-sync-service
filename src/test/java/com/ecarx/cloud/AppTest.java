package com.ecarx.cloud;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Unit test for simple DataAutoSyncServiceApp.
 */

@SpringBootTest(properties = "spring.profiles.active=local", classes = AutoSyncApp.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class AppTest 
{
    @Autowired
    private TransportClient transportClient;
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testAnalysis() {
        boolean result = false;
        String lexicalItem = "张柏芝爱了";
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
            System.out.println(e.getMessage());
        }
        System.out.println(result);
    }
}
