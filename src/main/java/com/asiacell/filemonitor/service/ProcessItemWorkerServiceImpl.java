package com.asiacell.filemonitor.service;
import com.asiacell.filemonitor.model.ProcessItem;
import com.asiacell.filemonitor.model.WorkerItem;
import com.asiacell.filemonitor.service.data.DataDaoService;
import com.asiacell.filemonitor.service.util.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ProcessItemWorkerServiceImpl implements ProcessItemWorkerService {
    static final ConcurrentLinkedQueue<WorkerItem>WORKER_ITEMS = new ConcurrentLinkedQueue<>();
    private static Logger LOGGER = LoggerFactory.getLogger( ProcessItemWorkerService.class);
    private Thread thread ;
    private boolean running = false;
    private String hostname;
    @Autowired
    UtilService utilService;
    @Autowired
    private DataDaoService dataDaoService;
    @PostConstruct
    public void init(){
        hostname  = utilService.getHostname();
    }
    @Override
    public void start() {
        thread = new Thread(this::run);
        running = true;
        thread.start();
    }

    public void process(WorkerItem workerItem) {
        List<ProcessItem> itemList = new ArrayList<>();
            for(String file:workerItem.getFiles()) {
                ProcessItem processItem  = new ProcessItem( utilService.guid(), hostname, workerItem.getMsisdn(), workerItem.getFolder(),file,0,new Date());
                itemList.add( processItem);
            }
            LOGGER.info("begin to add process item in db : "+itemList.size());
            addToDb( itemList);
    }


    @Override
    public void stop() {
        running = false;
    }


    @Override
    public void run() {
        while ( running ) {
            WorkerItem workerItem;
            if( (workerItem= WORKER_ITEMS.poll())!=null) {
                process( workerItem);
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int size() {
        return WORKER_ITEMS.size();
    }
    @Override
    public void add(WorkerItem workerItem) {
        WORKER_ITEMS.add( workerItem);

    }

    public void addToDb(List<ProcessItem>processItems) {
        LOGGER.info("add item to db : "+processItems.size());
        dataDaoService.addBatchProcessItems( processItems );
    }
}
