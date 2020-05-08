package com.ecarx.cloud.elasticsearch.task;

import com.ecarx.cloud.elasticsearch.event.IndexEvent;
import com.ecarx.cloud.elasticsearch.index.indexer.Indexer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;


@AllArgsConstructor
@Data
public class IndexerTask implements Serializable{
    private IndexEvent event;
    private Indexer indexer;
}
