package com.ecarx.cloud.cache;

import com.ecarx.cloud.task.IndexTask;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class CacheEntity implements Serializable{

    private Integer kind;
    private Set<String> words;
    private IndexTask task;
    private boolean directTask;


    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public Set<String> getWords() {
        return words;
    }

    public void setWords(Set<String> words) {
        this.words = words;
    }

    public IndexTask getTask() {
        return task;
    }

    public void setTask(IndexTask task) {
        this.task = task;
    }

    public boolean isDirectTask() {
        return directTask;
    }

    public void setDirectTask(boolean directTask) {
        this.directTask = directTask;
    }
}
