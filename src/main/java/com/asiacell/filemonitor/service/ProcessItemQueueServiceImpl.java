package com.asiacell.filemonitor.service;
import com.asiacell.filemonitor.model.ProcessItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
@Service
public class ProcessItemQueueServiceImpl  implements ProcessItemQueueService{
    private static ConcurrentLinkedQueue<ProcessItem> PROCESS_ITEMS = new ConcurrentLinkedQueue<>();
    private long currentTime  = System.currentTimeMillis();
    private boolean running = false;
    private Logger LOGGER = LoggerFactory.getLogger( ProcessItemQueueService.class);
    private Thread thread;
    @Override
    public void start() {
        running = true;
        thread   = new Thread(this::run);
        thread.start();
    }

    @Override
    public void stop() {
        running = false;
        if( thread.isAlive()) {
            try{
                thread.interrupt();
            }catch (Exception ex){

            }
        }
    }

    @Override
    public void run() {
        while ( running) {

            if( shouldAddItem()) {
                try {

                    addToDb();
                }catch (Exception ex){
                    LOGGER.info("error add to db ", ex);
                }
            }
        }
    }


    private void addToDb()
    {
        List<ProcessItem> items = new ArrayList<>();
        ProcessItem item;
        while (( item= PROCESS_ITEMS.poll())!=null) {
            items.add( item);
        }
        LOGGER.info("finish add to db");

    }

    private boolean shouldAddItem(){
        long diff = System.currentTimeMillis()  - currentTime;
        return  PROCESS_ITEMS.size() > 00 || ( PROCESS_ITEMS.size() > 0 && diff > 1000);
    }


    @Override
    public void add(ProcessItem item) {
        PROCESS_ITEMS.add( item);
    }

}
