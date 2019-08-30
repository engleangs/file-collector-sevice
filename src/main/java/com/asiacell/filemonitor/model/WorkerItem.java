package com.asiacell.filemonitor.model;

import java.util.Date;
import java.util.List;

public class WorkerItem {
    private String msisdn;
    private String folder;
    private List<String>files;
    private Date notifyDate;
    public WorkerItem(){

    }

    public WorkerItem(String msisdn, String folder, List<String> files, Date notifyDate) {
        this.msisdn = msisdn;
        this.folder = folder;
        this.files = files;
        this.notifyDate = notifyDate;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public Date getNotifyDate() {
        return notifyDate;
    }

    public void setNotifyDate(Date notifyDate) {
        this.notifyDate = notifyDate;
    }
}
