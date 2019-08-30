package com.asiacell.filemonitor;

import com.asiacell.filemonitor.service.FileShareFinalDirectoryService;
import com.asiacell.filemonitor.service.FileTrackService;
import com.asiacell.filemonitor.service.MovingFileWorkerService;
import com.asiacell.filemonitor.service.WatcherService;
import com.asiacell.filemonitor.service.data.DataDaoService;
import com.asiacell.filemonitor.service.test.PrepareTestService;
import com.asiacell.filemonitor.service.test.TestService;
import com.asiacell.filemonitor.service.util.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private static Logger LOGGER = LoggerFactory.getLogger( Application.class);
    @Autowired
    private WatcherService watcherService;
    @Autowired
    private FileTrackService fileTrackService;
    @Autowired
    private DataDaoService dataDaoService;
    @Autowired
    private UtilService utilService;
    @Autowired
    private TestService testService;
    @Autowired
    private FileShareFinalDirectoryService fileShareFinalDirectoryService;
    @Autowired
    private MovingFileWorkerService movingFileWorkerService;
    @Autowired
    private PrepareTestService prepareTestService;
     void test() throws IOException {
        String[] sourcePath = {"/Resource/Asiacell/data/077100606/","/Resource/Asiacell/data/077100607/"};
        String tmpPath = "/Resource/RD/docker/demo/filemonitor/tmp";
        String dataPath = "/Resource/RD/docker/demo/filemonitor/test_data";
        for(String path:sourcePath) {
            prepareTestService.prepareTestData( path, tmpPath , dataPath);
        }
    }

    void start() throws IOException {
        fileShareFinalDirectoryService.start();
        movingFileWorkerService.start();
    }
    void stop(){
        fileShareFinalDirectoryService.stop();
        movingFileWorkerService.stop();
    }
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("begin to watch for the directory");
        start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            LOGGER.info("begin to shutdown");
            stop();

        }));



    }
}
