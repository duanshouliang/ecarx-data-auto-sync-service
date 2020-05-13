package com.ecarx.cloud.business.music.test;

import com.ecarx.cloud.common.Result;
import com.ecarx.cloud.elasticsearch.index.IndexEvent;
import com.ecarx.cloud.elasticsearch.index.Indexer;
import org.elasticsearch.client.transport.TransportClient;


public class CanalTestTableIndexer extends Indexer {
    public CanalTestTableIndexer(TransportClient transportClient) {
        super.transportClient=transportClient;
    }

    @Override
    public Result<String> add(IndexEvent event) {
        return super.add(event);
    }

    @Override
    public Result<String> update(IndexEvent event) {
        return super.update(event);
    }

    @Override
    public Result<String> delete(IndexEvent event) {
        return super.delete(event);
    }
}
