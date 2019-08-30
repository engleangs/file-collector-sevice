package com.asiacell.filemonitor.service.test;

import com.asiacell.filemonitor.model.FileMoveItem;
import com.asiacell.filemonitor.model.ProcessItem;
import com.asiacell.filemonitor.service.FileShareFinalDirectoryService;
import com.asiacell.filemonitor.service.WatcherService;
import com.asiacell.filemonitor.service.data.DataDaoService;
import com.asiacell.filemonitor.service.util.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class TestService {
    @Autowired
    private UtilService utilService;
    @Autowired
    private WatcherService watcherService;
    @Autowired
    private DataDaoService dataDaoService;
    @Autowired
    private FileShareFinalDirectoryService fileShareFinalDirectoryService;
    private static Logger LOGGER = LoggerFactory.getLogger( TestService.class);
    public void testAddBatchProcessItem(){
        List<ProcessItem> testProcessItems = new ArrayList<>();
        testProcessItems.add( new ProcessItem(  utilService.guid() , utilService.getHostname(),"","07701105526","07701105526_PH_1222.jpg",1, new Date()));
        testProcessItems.add( new ProcessItem(  utilService.guid() , utilService.getHostname(),"","07701105527","07701105527_PH_1222.jpg",1, new Date()));
        testProcessItems.add( new ProcessItem(  utilService.guid() , utilService.getHostname(),"","07701105528","07701105528_PH_1222.jpg",1, new Date()));
        dataDaoService.addBatchProcessItems( testProcessItems );
    }

    public void testAddBatchFile() {
        List<FileMoveItem>fileMoveItems= new ArrayList<>();
        fileMoveItems.add( new FileMoveItem( new ProcessItem(  utilService.guid() , utilService.getHostname(),"","07701105526","07701105526_PH_1222.jpg",1, new Date()),1, new Date(),"demo/test/07701105526"));
        fileMoveItems.add( new FileMoveItem( new ProcessItem(  utilService.guid() , utilService.getHostname(),"","07701105527","07701105527_PH_1222.jpg",0, new Date()),2, new Date(),"demo/test/07701105527"));
        fileMoveItems.add( new FileMoveItem( new ProcessItem(  utilService.guid() , utilService.getHostname(),"","07701105528","07701105528_PH_1222.jpg",2, new Date()),3, new Date(),"demo/test/07701105527"));
        dataDaoService.addBatchFileItems( fileMoveItems );
    }

    public void testFileWatcher() throws IOException {
            watcherService.start();

    }
    public void testFileShaeFinal() throws IOException {
        fileShareFinalDirectoryService.start();
    }

    public void testCheckFileNotFound(){
        ProcessItem processItem = new ProcessItem( utilService.guid(), utilService.getHostname(),"","07701105526", "07701105526.jpg",1,new Date());
        int count = dataDaoService.checkFileNotFoundDb( utilService.getHostname(), processItem);
        LOGGER.info(" count : "+count);

    }

    public void testTrackFileNotFound(){
        ProcessItem processItem = new ProcessItem( utilService.guid(), utilService.getHostname(),"","07701105526", "07701105526.jpg",1,new Date());
        dataDaoService.trackFileNotFound( utilService.getHostname(), processItem);
    }
    public void testTrackOwnService(){
        dataDaoService.trackOwnService();
    }
    public void testGetAllServiceHost(){
        Set<String> result = dataDaoService.getAllServiceHost();
        for(String host:result) {
            LOGGER.info(" service : "+host);
        }
    }

    public  void testAddBatchFileItems(){
        ProcessItem processItem = new ProcessItem( "a7e8baa223d2489e842a0dca43dafb55", utilService.getHostname(),"","07701105526", "07701105526_test_111.jpg",1,new Date());
        FileMoveItem fileMoveItem = new FileMoveItem(processItem,1,new Date(),"final_path");
        dataDaoService.addBatchFileItems(Arrays.asList( fileMoveItem));
    }
}
