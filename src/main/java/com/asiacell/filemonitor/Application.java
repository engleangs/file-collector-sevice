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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
     void test(String[] args) throws IOException {
        String sourcePath = args[1];
        String tmpPath = args[2];
        String dataPath = args[3];
         prepareTestService.prepareTestData( sourcePath, tmpPath , dataPath);

    }

    void start() throws IOException {
         dataDaoService.trackOwnService();
        fileShareFinalDirectoryService.start();
        movingFileWorkerService.start();
    }
    void stop(){
        fileShareFinalDirectoryService.stop();
        movingFileWorkerService.stop();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    void copy(String source, String dest) throws IOException {
        System.out.println("copy "+source +" to "+dest);
        Files.copy(Paths.get( source) , Paths.get( dest));
    }
    void mkdir(String source,String folderName) {
        System.out.println("mdkdir "+source +" to "+folderName);
        File file = new File( source +"/"+folderName);
        if( !file.exists()) {
            boolean result = file.mkdirs();
            System.out.println( result);
        }
    }
    void  rename(String source, String dest){
        System.out.println("rename "+source +" to "+dest);
        File sourceFIle = new File( source);
        boolean result = sourceFIle.renameTo( new File( dest));
        System.out.println("result " +result);
    }
    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("begin to watch for the directory");
        if(args.length > 0) {
            if("test".equals( args[0])) {
                test(args);
                return;
            }
            else if("copy".equals(args[0])) {
                copy( args[1], args[2]);
            }
            else if("mkdir".equals(args[0])) {
                mkdir( args[1], args[2]);
            }
            else if("rename".equals( args[0])) {
                rename( args[1], args[2]);
            }
            return;

        }
        start();
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            LOGGER.info("begin to shutdown");
            stop();

        }));



    }
}
