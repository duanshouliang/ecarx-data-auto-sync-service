package com.ecarx.cloud.business.music.factory;

import com.ecarx.cloud.business.music.test.CanalTestTableIndexer;
import com.ecarx.cloud.elasticsearch.index.Indexer;
import org.elasticsearch.client.transport.TransportClient;

public class IndexerSelector {
    public static Indexer select(String business, TransportClient transportClient){
        return getInstance(business, transportClient);
    }

    private static Indexer getInstance(String business, TransportClient transportClient){
        Indexer indexer = null;
        switch (business){
            case "kuwo_music.canal_test":
                indexer = new CanalTestTableIndexer(transportClient);
                break;
            default:
                break;
        }
        return indexer;
    }
}
