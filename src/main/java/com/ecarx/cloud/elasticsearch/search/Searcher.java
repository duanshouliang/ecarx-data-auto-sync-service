package com.ecarx.cloud.elasticsearch.search;

import com.alibaba.fastjson.JSONObject;

import java.util.List;

public abstract class Searcher {
    public List<JSONObject> queryByCondition(SearchEvent event){
        return null;
    }
}
