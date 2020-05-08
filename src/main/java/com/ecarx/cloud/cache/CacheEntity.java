package com.ecarx.cloud.cache;

import com.ecarx.cloud.elasticsearch.task.IndexerTask;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class CacheEntity implements Serializable{

    private String cp;
    private List<String> words;
    private List<IndexerTask> tasks;


    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public List<IndexerTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<IndexerTask> tasks) {
        this.tasks = tasks;
    }
}
