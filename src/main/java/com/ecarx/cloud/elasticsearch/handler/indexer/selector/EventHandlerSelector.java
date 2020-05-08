package com.ecarx.cloud.elasticsearch.handler.indexer.selector;


import com.ecarx.cloud.elasticsearch.handler.indexer.CanalTestIndexEventHandler;
import com.ecarx.cloud.elasticsearch.handler.indexer.IndexEventHandler;

public class EventHandlerSelector {

    public static IndexEventHandler selector(String business){
        return getInstance(business);
    }

    private static IndexEventHandler getInstance(String business){
        IndexEventHandler handler = null;
        switch (business){
            case "kuwo_music_canal_test":
                handler = new CanalTestIndexEventHandler();
                break;
            default:
                break;
        }
        return handler;
    }
}
