package com.ecarx.cloud.business.music.factory;


import com.ecarx.cloud.elasticsearch.index.IndexEventHandler;

public class EventHandlerSelector {

    public static IndexEventHandler selector(String business){
        return getInstance(business);
    }

    private static IndexEventHandler getInstance(String business){
        IndexEventHandler handler = null;
        return new IndexEventHandler();
    }
}
