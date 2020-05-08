package com.ecarx.cloud.cache;

import com.ecarx.cloud.task.IndexTask;

import java.io.Serializable;
import java.util.List;

public class CacheEntity implements Serializable{

    private String cp;
    private List<String> words;
    private List<IndexTask> tasks;


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

    public List<IndexTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<IndexTask> tasks) {
        this.tasks = tasks;
    }
}
