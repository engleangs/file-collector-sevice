package com.asiacell.filemonitor.model;

import java.util.Date;

public class ProcessItem {
    private String guid;
    private String app;
    private String fromHostname;
    private String folder;
    private String misisdn;
    private String fileName;
    private int status;
    private Date addDate;
    private String tempPath;
    private String tempGuid;
    private String description;
    private int retryTime  = 0;
    private Date startMoveFileDate;
    private Date expectedRunDate;
    public ProcessItem(){

    }


    public ProcessItem(String guid, String fromHostname, String folder, String misisdn, String fileName, int status, Date addDate) {

        this.fromHostname = fromHostname;
        this.folder = folder;
        this.misisdn = misisdn;
        this.fileName = fileName;
        this.status = status;
        this.addDate = addDate;
        this.guid = guid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFromHostname() {
        return fromHostname;
    }

    public void setFromHostname(String fromHostname) {
        this.fromHostname = fromHostname;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMisisdn() {
        return misisdn;
    }

    public void setMisisdn(String misisdn) {
        this.misisdn = misisdn;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public String getTempGuid() {
        return tempGuid;
    }

    public void setTempGuid(String tempGuid) {
        this.tempGuid = tempGuid;
    }

    public String getDescription() {
        return description;
    }

    public int getRetryTime() {
        return retryTime;
    }
    public void retry(){
        this.retryTime++;
    }

    public Date getStartMoveFileDate() {
        return startMoveFileDate;
    }

    public void setStartMoveFileDate(Date startMoveFileDate) {
        this.startMoveFileDate = startMoveFileDate;
    }

    public Date getExpectedRunDate() {
        return expectedRunDate;
    }

    public void setExpectedRunDate(Date expectedRunDate) {
        this.expectedRunDate = expectedRunDate;
    }
}
