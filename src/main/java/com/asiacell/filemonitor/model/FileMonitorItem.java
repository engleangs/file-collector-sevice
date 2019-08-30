package com.asiacell.filemonitor.model;

import java.util.Date;

public class FileMonitorItem {
    private String fileName;
    private String fullPath;
    private String hostname;
    private Date noticeDate;
    private Date modifiedDate;
    private int status;
    public FileMonitorItem(){

    }

    public FileMonitorItem( String fileName, String fullPath, String hostname, Date noticeDate, Date modifiedDate, int status) {
        this.fileName = fileName;
        this.fullPath = fullPath;
        this.hostname = hostname;
        this.noticeDate = noticeDate;
        this.modifiedDate = modifiedDate;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Date getNoticeDate() {
        return noticeDate;
    }

    public void setNoticeDate(Date noticeDate) {
        this.noticeDate = noticeDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
