package com.asiacell.filemonitor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProcessingTaskFile implements Runnable {

    private String basePath;
    private String destPath;
    private String fileName;
    static Logger LOGGER = LoggerFactory.getLogger( ProcessingTaskFile.class);

    public ProcessingTaskFile(String basePath, String destPath, String fileName) {
        this.basePath = basePath;
        this.destPath = destPath;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String today = simpleDateFormat.format( new Date());
        File destFolder = new File( destPath +"/"+today);
        if( !destFolder.exists() ) {
            destFolder.mkdirs();
        }
        File destFile = new File( destPath +"/"+today+"/"+fileName);
        File sourceFile = new File( basePath+"/"+fileName);
        boolean result = sourceFile.renameTo( destFile);
        LOGGER.info("rename file from :"+basePath+"/"+fileName +" to "+destPath+"/"+today+"/"+fileName +" success : "+result);

    }
}
