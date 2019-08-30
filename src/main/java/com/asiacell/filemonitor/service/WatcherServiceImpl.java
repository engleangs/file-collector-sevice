package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.FileMonitorItem;
import com.asiacell.filemonitor.service.util.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.Date;

@Service
public class WatcherServiceImpl implements WatcherService {
    private WatchService watchService;
    @Value("${file.tmp-dir}")
    private String watchDirectory;
    @Value("${file.process-dir}")
    private String processDirectory;
    private boolean running = false;
    private static Logger LOGGER = LoggerFactory.getLogger( WatcherService.class);
    private Thread thread;

    @Autowired
    private FileTrackService fileTrackService;
    @Autowired
    private UtilService utilService;
    @Override
    public void start() throws IOException {
         watchService = FileSystems.getDefault().newWatchService();
        Path path = Paths.get( watchDirectory);
        WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        LOGGER.info(watchKey.toString());
        this.running = true;
        thread = new Thread(()->{
            try {
                run();
            } catch (IOException e) {
                LOGGER.error("run error ", e);
            }
        });
        thread.start();

    }

    @Override
    public void stop() throws IOException {
        watchService.close();
        running = false;
        if( thread.isAlive()) {
            try{
                Thread.sleep(100);
                thread.interrupt();
            }catch (Exception e){

            }
        }
    }

    @Override
    public void run() throws IOException {
        String hostname = utilService.getHostname();
        while (running) {
           // LOGGER.info("");
            WatchKey watchKey;
            try {
            while ((watchKey = watchService.take()) != null) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    LOGGER.info("receive file Event kind:" + event.kind() +" , "+" file affected :"+event.context());
                    fileTrackService.add(new FileMonitorItem( event.context().toString(),
                            watchDirectory+"/"+event.context().toString(),
                            hostname,
                            new Date(),
                            new Date(),
                            0
                    ));
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
    public int size() {
        return 0;
    }
}
