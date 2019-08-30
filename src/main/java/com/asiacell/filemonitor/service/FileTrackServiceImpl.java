package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.FileMonitorItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
///todo:implement for restarting or start with existing in the folder
@Service
public class FileTrackServiceImpl implements FileTrackService {
    private static final ConcurrentLinkedQueue<FileMonitorItem> FILE_MONITOR_ITEMS = new ConcurrentLinkedQueue<>();
    private Thread thread;
    private boolean running = false;
    private long currentTime = System.currentTimeMillis();
    private static final Logger LOGGER = LoggerFactory.getLogger( FileTrackService.class);

    @Override
    public void add(FileMonitorItem item) {
        FILE_MONITOR_ITEMS.add( item);
    }

    @Override
    public void start() {
        running = true;
        thread = new Thread(()->{
            run();
        });
        thread.start();
    }

    private boolean shouldAddItem(){
        long diff = System.currentTimeMillis()  - currentTime;
        return  FILE_MONITOR_ITEMS.size() > 00 || ( FILE_MONITOR_ITEMS.size() > 0 && diff > 1000);
    }

    private void run(){

        while (running) {

            if( shouldAddItem() ) {
                LOGGER.info("begin to track file item into db");
                addToDb();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error("error",e);
            }
        }
    }

    private void addToDb()
    {
        FileMonitorItem item;
        List<FileMonitorItem> items = new ArrayList<>();
        while (( item= FILE_MONITOR_ITEMS.poll())!=null) {
            items.add( item);
        }
        LOGGER.info("finish adding item to db");
    }


    @Override
    public void stop() {
        running  = false;
        if( thread.isAlive()) {
            try{
                thread.interrupt();
            }catch (Exception ex){

            }
        }
    }

    @Override
    public int size() {
        return FILE_MONITOR_ITEMS.size();
    }
}
