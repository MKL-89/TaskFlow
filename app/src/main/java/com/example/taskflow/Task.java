package com.example.taskflow;

public class Task {
    private String title;
    private String priority;
    private boolean isCompleted;
    private long id;

    public Task(String title, String priority) {
        this.title = title;
        this.priority = priority;
        this.isCompleted = false;
        this.id = System.currentTimeMillis();
    }

    public Task(long id, String title, String priority, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.isCompleted = isCompleted;
    }

    public String getTitle()       { return title; }
    public String getPriority()    { return priority; }
    public boolean isCompleted()   { return isCompleted; }
    public long getId()            { return id; }

    public void setTitle(String title)          { this.title = title; }
    public void setPriority(String priority)    { this.priority = priority; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }
}