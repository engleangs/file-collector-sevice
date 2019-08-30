package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.FileMoveItem;
import com.asiacell.filemonitor.model.ProcessItem;
import com.asiacell.filemonitor.service.data.DataDaoService;
import com.asiacell.filemonitor.service.util.FileHelperService;
import com.asiacell.filemonitor.service.util.UtilService;
import com.asiacell.filemonitor.task.FileMoveTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
@Service
public class MovingFileWorkerServiceImpl implements MovingFileWorkerService {
    ThreadPoolExecutor threadPoolExecutor;
    @Value("${worker-number}")
    private int workerNumber;
    static final Logger LOGGER = LoggerFactory.getLogger( MovingFileWorkerService.class);
    static final ConcurrentLinkedQueue<FileMoveItem>QUEUE = new ConcurrentLinkedQueue<>();
    @Autowired
    private DataDaoService dataDaoService;
    @Autowired
    private FileHelperService fileHelperService;
    @Value("${file.destination}")
    private String destination;
    @Value("${file.tmp-dir}")
    private String basePath;
    @Value("${file.retry-on-fail}")
    private String retiesList;
    private boolean running = false;


    private Thread checkingThread;
    private Thread trackingThread;
    @Autowired
    private UtilService utilService ;

    private long startTrackDb = System.currentTimeMillis();
    private Set<String> hostOfServices;
    private  FileMoveTask.FileMoveConfig fileMoveConfig;

    @Override
    public void start() {
        this.running = true;
        hostOfServices = dataDaoService.getAllServiceHost();
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(  workerNumber );
        checkingThread = new Thread(this::run);
        checkingThread.start();
        trackingThread = new Thread(this::trackingDb);
        trackingThread.start();
        List<Long>retries = new ArrayList<>();
        String[] retryStr = retiesList.split(",");
        for(String str:retryStr) {
            retries.add( Long.parseLong( str));
        }
        fileMoveConfig = new FileMoveTask.FileMoveConfig( retries, destination, this.basePath);
    }

    @Override
    public void stop() {
        threadPoolExecutor.shutdown();
    }

    public boolean  fileNotExist(ProcessItem processItem) {
        try{
            String path = basePath+"/"+utilService.pathOfMsisdn( processItem.getMisisdn())+"/"+processItem.getFileName();
            LOGGER.info("begin to check for the file");
            File file = new File(path);
            String hostname = utilService.getHostname();
            if(!file.exists()) {
                dataDaoService.trackFileNotFound( hostname, processItem);
                return true;
            }
            return false;
        }catch (Exception ex){
            LOGGER.error("check file ",ex);
        }
        return false;
    }


    private void checkIfAllNotFoundAndMoveOut(ProcessItem processItem) {
        List<String> hosts = dataDaoService.getAllHostForFileNotFound( processItem);
        Set<String>notFoundHost = new HashSet<>( hosts);
        if( notFoundHost.size() == hostOfServices.size() && notFoundHost.containsAll( hostOfServices)) {
            dataDaoService.moveToNotFound( processItem);
        }

    }

    @Override
    public void run() {
        while ( running ) {
            List<ProcessItem> items = dataDaoService.getListItem();
            if( items.size() > 0) {
                for(ProcessItem item : items )
                {
                    LOGGER.info("begin to execute moving task :"+item.getFileName()+","+item.getApp());
                    if( fileNotExist( item)) {
                        checkIfAllNotFoundAndMoveOut( item);
                    }
                    else {
                        threadPoolExecutor.execute( new FileMoveTask( utilService, this.fileMoveConfig, fileHelperService, item,this));
                    }
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                LOGGER.debug("error on run ", e);

            }
        }
    }

    private boolean shouldAddToDb(){
        long diff = System.currentTimeMillis() - startTrackDb;
        return QUEUE.size() > 0 && diff > 200;
    }
    private void addToDb(){
        List<FileMoveItem>items = new ArrayList<>();
        FileMoveItem item;
        while ((item = QUEUE.poll())!= null) {
            items.add( item );
        }
        if( items.size() > 0 ) {
            LOGGER.info("begin to add to db :"+items.size());
            dataDaoService.addBatchFileItems( items);
            LOGGER.info("finish batch add to db :"+items.size());
        }
    }

    @Override
    public void track(FileMoveItem fileMoveItem) {
        QUEUE.add( fileMoveItem);
    }
    public void trackingDb()
    {
        while (this.running) {

            if( shouldAddToDb()) {
                addToDb();
                this.startTrackDb = System.currentTimeMillis();
                LOGGER.info("finish tracking file to final directory..");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.debug("tracking db ",e);
            }
        }
    }
}
