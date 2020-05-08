package com.ecarx.cloud.elasticsearch.query;

import com.alibaba.fastjson.JSONObject;
import com.ecarx.cloud.elasticsearch.event.SearchEvent;
import java.util.List;

public abstract class Searcher {
    public List<JSONObject> queryByCondition(SearchEvent event){
        return null;
    }
}
