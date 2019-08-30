package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.ProcessItem;
import com.asiacell.filemonitor.model.WorkerItem;
import com.asiacell.filemonitor.service.data.DataDaoService;
import com.asiacell.filemonitor.service.util.UtilService;
import com.asiacell.filemonitor.task.ProcessingTaskFile;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

///todo:implement for starting after file watcher service or restart
@Service
public class FileShareFinalDirectoryServiceImpl implements FileShareFinalDirectoryService {

    @Value("${monitor-directory}")
    private String monitorDirectory;
    @Value("${task-directory}")
    private String taskDirectory;
    private static final Logger LOGGER = LoggerFactory.getLogger( FileShareFinalDirectoryService.class);
    private Thread thread;
    private Thread dbThread;
    private boolean running;
    private WatchService watchService;
    private Gson gson = new Gson();
    private static ConcurrentLinkedQueue<WorkerItem> DB_QUEUE = new ConcurrentLinkedQueue<>();
    private ThreadPoolExecutor threadPoolExecutor ;
    private int MAX_QUEUE = 10;
    @Autowired
    private UtilService utilService;
    @Autowired
    private DataDaoService dataDaoService;
    private long dbStartTime = System.currentTimeMillis();


    @Override
    public void start() throws IOException {
        LOGGER.info("start share final directory");
        watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get( monitorDirectory);
        loadOldFile();
        WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        LOGGER.info(watchKey.toString());
        running = true;
        thread = new Thread(this::run);
        thread.start();
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
        dbThread = new Thread(this::dbRun);
        dbThread.start();

    }

    private void loadOldFile()
    {
        File file = new File( monitorDirectory);
        LOGGER.info("Begin to look for existing action file ...");
        for(File actionFile : file.listFiles()) {
            LOGGER.info(" loading old action file :"+actionFile.getName());
            prepareCmd( actionFile.getName());
        }
    }

    public void dbRun(){
        while ( running ){
            try {
                long diff = System.currentTimeMillis() - dbStartTime;
                if( DB_QUEUE.size() > MAX_QUEUE || (DB_QUEUE.size() > 0 && diff > 200)) {
                    addToDb();
                    dbStartTime = System.currentTimeMillis();
                }
                Thread.sleep(200);
            }catch (Exception e){
                LOGGER.error("dbRun error",e);
            }

        }
    }


    private void prepareCmd(String fileName) {
        try {
            String content = new String ( Files.readAllBytes( Paths.get(monitorDirectory+"/"+fileName) ) );
            WorkerItem workerItem = gson.fromJson( content, WorkerItem.class);
            workerItem.setNotifyDate( new Date());
            DB_QUEUE.add( workerItem);
            if(DB_QUEUE.size() > MAX_QUEUE) {
                addToDb();
            }

        }catch (IOException e)
        {
            LOGGER.error("error",e);
        }catch (Exception e) {
            LOGGER.error("File cmd read error ", e);
        }

    }


    private void addToDb(){
        List<ProcessItem>items = new ArrayList<>();
        List<WorkerItem>workerItems  = new ArrayList<>();
        WorkerItem workerItem;
        String hostname = utilService.getHostname();
        while ((workerItem  = DB_QUEUE.poll())!=null) {
            workerItems.add( workerItem);
            for(String fileName : workerItem.getFiles()) {
                ProcessItem processingFileItem = new ProcessItem( utilService.guid(),hostname,   workerItem.getFolder(), workerItem.getMsisdn(),fileName,0 , new Date());
                items.add( processingFileItem );
            }
        }
        dataDaoService.addBatchProcessItems(  items );
        for(WorkerItem fileWorker:workerItems) {

            ProcessingTaskFile processingTaskFile = new ProcessingTaskFile(monitorDirectory,taskDirectory, fileWorker.getMsisdn()+".txt");
            threadPoolExecutor.execute( processingTaskFile);
        }
    }

    @Override
    public void stop() {
        running = false;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if( thread.isAlive()) {
            thread.interrupt();
        }
        if( dbThread.isAlive()) {
            dbThread.interrupt();
        }
    }

    @Override
    public void run() {
            while ( running) {
                WatchKey watchKey;
                try {
                    while ((watchKey = watchService.take()) != null) {
                        for (WatchEvent<?> event : watchKey.pollEvents()) {
                            LOGGER.info("receive file Event kind:" + event.kind() +" , "+" file affected :"+event.context());
                            prepareCmd( event.context().toString());
                        }
                        watchKey.reset();
                    }
                } catch (InterruptedException   e) {
                    LOGGER.error("error watch directory :",e);
                }catch (Exception ex){
                    LOGGER.error("error watch directory :",ex);
                }
            }
    }

    @Override
    public void size() {
        return;
    }
}
