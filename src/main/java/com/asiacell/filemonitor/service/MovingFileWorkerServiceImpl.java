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
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

@Service
public class MovingFileWorkerServiceImpl implements MovingFileWorkerService {
    ThreadPoolExecutor threadPoolExecutor;
    @Value("${worker-number}")
    private int workerNumber;
    static final Logger LOGGER = LoggerFactory.getLogger( MovingFileWorkerService.class);
    static final ConcurrentHashMap<String,FileMoveItem>QUEUE = new ConcurrentHashMap<>();

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
    private int counter = 0;
    private Thread checkingThread;
    private Thread trackingThread;
    private Thread retryThread;
    @Value("${file.max-queue-retry}")
    private int maxSizeRetryQueue;
    @Autowired
    private UtilService utilService ;
    private long startTrackDb = System.currentTimeMillis();
    private Set<String> hostOfServices;
    private  FileMoveTask.FileMoveConfig fileMoveConfig;
    private ConcurrentHashMap<String,ProcessItem>processingQueue =  new ConcurrentHashMap<>();
    private  ConcurrentHashMap<String,ProcessItem>retryQueue = new ConcurrentHashMap<>();

    @Override
    public void start() {
        this.running = true;
        hostOfServices = dataDaoService.getAllServiceHost();
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(  workerNumber );
        checkingThread = new Thread(this::run);
        checkingThread.start();
        trackingThread = new Thread(this::trackingDb);
        trackingThread.start();
        List<Integer>retries = new ArrayList<>();
        String[] retryStr = retiesList.split(",");
        for(String str:retryStr) {
            retries.add( Integer.parseInt( str));
        }
        fileMoveConfig = new FileMoveTask.FileMoveConfig( retries, destination, this.basePath);
        retryThread = new Thread(this::monitorRetry);
        retryThread.start();
        LOGGER.info("Starting file worker success  ");
        LOGGER.info(" Temp directory  : "+basePath);
        LOGGER.info(" Final directory :"+destination);
        LOGGER.info(" File worker thread :"+workerNumber);
        LOGGER.info(" MAX RETRY QUEUE : "+maxSizeRetryQueue);
    }

    @Override
    public void stop() {
        running = false;
        threadPoolExecutor.shutdown();
        try{
            Thread.sleep(100);
            if(retryThread.isAlive()) {
                retryThread.interrupt();
            }
            if( trackingThread.isAlive()) {
                trackingThread.interrupt();
            }
            if(checkingThread.isAlive()) {
                checkingThread.interrupt();
            }
        }catch (Exception ex){

        }


    }

    public boolean  fileNotExist(ProcessItem processItem) {
        try{
            String path = basePath+"/"+utilService.pathOfMsisdn( processItem.getMisisdn())+"/"+processItem.getFileName();
            LOGGER.info("begin to check for the file if exist : "+path);
            File file = new File(path);
            String hostname = utilService.getHostname();
            if(!file.exists()) {
                LOGGER.info("file not found in this host : "+hostname+" , "+processItem.getFileName());
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
            LOGGER.info("move out this item : "+processItem.getGuid()+","+processItem.getFileName());
            dataDaoService.moveToNotFound( processItem);
        }

    }

    Set<String>filterGuiId(){
        Set<String>hashSet = new HashSet<>();
        hashSet.addAll( processingQueue.keySet());
        hashSet.addAll( QUEUE.keySet());
        hashSet.addAll( retryQueue.keySet());
        return hashSet;

    }

    @Override
    public void run() {
        while ( running ) {

            if( counter > 100) {
                LOGGER.info("processing queue for moving file:"+processingQueue.size() +", retry queue :"+retryQueue.size() +", db queue :"+QUEUE.size());
                counter = 0;
            }
            try {
                List<ProcessItem> items = dataDaoService.getListItem( filterGuiId());
                LOGGER.info(" size of processing  item : " + items.size() +", retry queue :"+retryQueue.size() +", db queue :"+QUEUE.size());
                if (items.size() > 0) {
                    for (ProcessItem item : items) {
                        if (item.getFileName() == null) {
                            item.setDescription("invalid file name");
                            item.setStatus(0);
                            FileMoveItem fileMoveItem = new FileMoveItem(item, 0, new Date(), "", new Date(), new Date());
                            track(fileMoveItem);
                            LOGGER.info("invalid file name");
                            continue;
                        }
                        if (processingQueue.containsKey(item.getGuid())) {
                            LOGGER.info(" processing queue : " + processingQueue.size());
                            LOGGER.info(" " + item.getFileName() + " in the queue already");
                            continue;
                        }
                        if( retryQueue.containsKey( item.getGuid())) {
                            LOGGER.info(" retry queue : "+retryQueue.size());
                            LOGGER.info(" " + item.getFileName() + " in the retry-queue already");
                            continue;
                        }
                        LOGGER.info("begin to execute moving task :" + item.getFileName());
                        if (fileNotExist(item)) {
                            checkIfAllNotFoundAndMoveOut(item);
                        } else {
                            LOGGER.info("begin to process for : " + item.getGuid());
                            item.setStartMoveFileDate(new Date());
                            threadPoolExecutor.execute(new FileMoveTask(utilService, this.fileMoveConfig, fileHelperService, item, this));
                            processingQueue.put(item.getGuid(), item);
                        }
                    }
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    LOGGER.debug("error on run ", e);

                }
            }catch (CannotGetJdbcConnectionException ex){
                LOGGER.error("db closed ",ex);
                return;
            }
            catch (Exception ex){
                LOGGER.error("error",ex);
            }
            counter++;
        }
    }

    private boolean shouldAddToDb(){
        long diff = System.currentTimeMillis() - startTrackDb;
        return QUEUE.size() > 0 && diff > 200;
    }
    private void addToDb(){
        List<FileMoveItem>items = new ArrayList<>();
        for(String key : QUEUE.keySet()) {

            FileMoveItem fileMoveItem =    QUEUE.get(key);
            items.add( fileMoveItem);
        }

        if( items.size() > 0 ) {
            LOGGER.info("begin to add to db :"+items.size());
            dataDaoService.addBatchFileItems( items);
            LOGGER.info("finish batch add to db :"+items.size());
        }
        for(FileMoveItem item : items) {
            QUEUE.remove( item.getProcessItem().getGuid());//remove it out
            LOGGER.info("clean from db queue :"+item.getProcessItem().getGuid());
        }
    }

    @Override
    public void track(FileMoveItem fileMoveItem) {
        QUEUE.put( fileMoveItem.getProcessItem().getGuid(), fileMoveItem);
         ProcessItem item =  processingQueue.remove( fileMoveItem.getProcessItem().getGuid());
         retryQueue.remove( fileMoveItem.getProcessItem().getGuid());
         if( item != null) {
             LOGGER.info(" finish for this item : "+item.getGuid() +" ->"+fileMoveItem.getProcessItem().getFileName() ) ;
         }

        LOGGER.info(" remaining in db queue : "+QUEUE.size() +" , retry queue "+ processingQueue.size());
        if(QUEUE.size() > 100) {
            addToDb();
        }


    }

    public void trackingDb()
    {
        while (this.running) {

            LOGGER.info(" Tracking db -> should add to db  for queue : "+shouldAddToDb());
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



    @Override
    public boolean isRetryQueueAvailable() {
        return processingQueue.size() < this.maxSizeRetryQueue;
    }

    @Override
    public void putInretry(ProcessItem processItem) {
        processingQueue.remove( processItem.getGuid());
        LOGGER.info("put in retry : "+processItem.getGuid()+"->"+processItem.getFileName());
        retryQueue.put( processItem.getGuid(), processItem);
    }


    public void monitorRetry() {
            while (running) {
                try {
                    for(String key : retryQueue.keySet())
                    {
                        ProcessItem processItem = retryQueue.get( key);
                        if( processItem == null) {
                            LOGGER.info("not found ");
                            continue;
                        }
                        if( processItem.isRunning()) {
                            LOGGER.info(" it's running for this : "+processItem.getGuid());
                            continue;
                        }
                        if (processItem.getExpectedRunDate().getTime() <= System.currentTimeMillis()) {
                            LOGGER.info("retry now for this : " + processItem.getGuid() + ", " + processItem.getFileName() +" for : "+processItem.getRetryTime() +" times");
                            threadPoolExecutor.execute(new FileMoveTask(utilService, this.fileMoveConfig, fileHelperService, processItem, this));
                            processingQueue.put( processItem.getGuid(), processItem);//add back to processing queue
                        }
                    }
                    LOGGER.info(" total retry items :"+retryQueue.size());

                }catch (NoSuchElementException ex){
                    //ignore this
                    LOGGER.info("no retry item ");
                }
                try {
                    Thread.sleep(500);
                }catch (Exception ex){
                    LOGGER.error("retry issue ",ex);
                }
            }
    }
}
