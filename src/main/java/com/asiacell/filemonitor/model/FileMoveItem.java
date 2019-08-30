package com.asiacell.filemonitor.model;

import java.util.Date;

public class FileMoveItem {
    private ProcessItem processItem;
    private int retryTime;
    private Date lastActionDate;
    private String finalPath;
    public FileMoveItem(ProcessItem processItem, int retryTime, Date lastActionDate, String finalPath){
        this.processItem  = processItem;
        this.retryTime = retryTime;
        this.lastActionDate  = lastActionDate;
        this.finalPath = finalPath;
    }

    public ProcessItem getProcessItem() {
        return processItem;
    }

    public void setProcessItem(ProcessItem processItem) {
        this.processItem = processItem;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    public Date getLastActionDate() {
        return lastActionDate;
    }

    public void setLastActionDate(Date lastActionDate) {
        this.lastActionDate = lastActionDate;
    }

    public String getFinalPath() {
        return finalPath;
    }

    public void setFinalPath(String finalPath) {
        this.finalPath = finalPath;
    }
}
