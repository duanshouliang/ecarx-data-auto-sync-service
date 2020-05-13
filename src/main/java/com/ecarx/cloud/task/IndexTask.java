package com.ecarx.cloud.task;

import com.ecarx.cloud.elasticsearch.index.IndexEvent;
import com.ecarx.cloud.elasticsearch.index.Indexer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;


@AllArgsConstructor
@Data
public class IndexTask implements Serializable{
    private IndexEvent event;
    private Indexer indexer;
}
