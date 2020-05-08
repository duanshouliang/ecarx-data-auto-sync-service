package com.ecarx.cloud.elasticsearch.index.indexer;

import com.alibaba.fastjson.JSONObject;
import com.ecarx.cloud.common.Result;
import com.ecarx.cloud.elasticsearch.event.IndexEvent;
import com.ecarx.cloud.elasticsearch.util.ScriptUtil;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class Indexer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Indexer.class);

    public TransportClient transportClient;

    public Result<String> add(IndexEvent event){
        String topic = event.getTopic();
        IndexRequestBuilder requestBuilder = transportClient.prepareIndex(topic, event.getType())
                .setSource(JSONObject.toJSONString(event.getFields()), XContentType.JSON);

        IndexResponse response = null;
        try{
            response = requestBuilder.get();
        }catch (Exception e){
            LOGGER.error("Bulk indexer for {} with exception {}, stack {}", topic, e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        if(response.status() == RestStatus.CREATED){
            return Result.success();
        }else{
            return Result.failed("Add new Document failed");
        }
    }

    public Result<String> update(IndexEvent event){
        String topic = event.getTopic();
        Map<String, Object> fields = event.getFields();
        BoolQueryBuilder queryBuilder = this.buildFilter(event.getCondition());
        Script script = ScriptUtil.buildScript(fields);
        BulkByScrollResponse response = null;
        if(null != script && null != queryBuilder){
            UpdateByQueryRequestBuilder builder = UpdateByQueryAction.INSTANCE.newRequestBuilder(transportClient)
                    .source(topic)
                    .script(script)
                    .filter(queryBuilder);
            response = builder.get();
        }
        if(response == null || (response.getBulkFailures()!=null && response.getBulkFailures().size() !=0)){
            return Result.failed(response.getBulkFailures().get(0).getMessage());
        }else{
            return Result.success();
        }
    }

    public Result<String> delete(IndexEvent event){
        String topic = event.getTopic();
        BoolQueryBuilder queryBuilder = this.buildFilter(event.getCondition());
        if(null == queryBuilder){
            return Result.failed("Must specify delete filters");
        }
        DeleteByQueryRequestBuilder deleteByQueryRequest = DeleteByQueryAction
                    .INSTANCE
                    .newRequestBuilder(transportClient)
                    .source(topic)
                    .filter(queryBuilder);
        BulkByScrollResponse response = deleteByQueryRequest.get();
        List<BulkItemResponse.Failure> failures = response.getBulkFailures();
        if(failures !=null && failures.size() != 0){
            return Result.failed("Delete by query with condition + " + event.getCondition() +" failed");
        }else{
            return Result.success();
        }
    }
    private BoolQueryBuilder buildFilter(Map<String, Object> filterCondition){
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        filterCondition.forEach((field, value)->{
            queryBuilder.must(QueryBuilders.termQuery(field, value));
        });
        return queryBuilder;
    }
}
