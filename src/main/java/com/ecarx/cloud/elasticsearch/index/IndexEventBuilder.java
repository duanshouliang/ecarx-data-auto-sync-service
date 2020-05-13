package com.ecarx.cloud.elasticsearch.index;


public class IndexEventBuilder {
    public static IndexEventHandler build(String business){
        return getIndexEventHandlerInstance();
    }

    public static IndexEventHandler getIndexEventHandlerInstance(){
        IndexEventHandler handler = null;
        return handler;
    }
}
