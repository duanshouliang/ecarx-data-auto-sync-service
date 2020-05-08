package com.ecarx.cloud.elasticsearch.index.indexer.selector;

import com.ecarx.cloud.elasticsearch.index.indexer.CanalTestTableIndexer;
import com.ecarx.cloud.elasticsearch.index.indexer.Indexer;
import org.elasticsearch.client.transport.TransportClient;

public class IndexerSelector {
    public static Indexer select(String business, TransportClient transportClient){
        return getInstance(business, transportClient);
    }

    private static Indexer getInstance(String business, TransportClient transportClient){
        Indexer indexer = null;
        switch (business){
            case "kuwo_music_canal_test":
                indexer = new CanalTestTableIndexer(transportClient);
                break;
            default:
                break;
        }
        return indexer;
    }
}
