package com.asiacell.filemonitor.task;

import com.asiacell.filemonitor.model.FileMoveItem;
import com.asiacell.filemonitor.model.ProcessItem;
import com.asiacell.filemonitor.service.MovingFileWorkerService;
import com.asiacell.filemonitor.service.util.FileHelperService;
import com.asiacell.filemonitor.service.util.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class FileMoveTask implements Runnable {

    private static Logger LOGGER = LoggerFactory.getLogger( FileMoveTask.class);
    private ProcessItem processItem;
    private FileHelperService fileHelperService;
    private MovingFileWorkerService movingFileWorkerService;
    private FileMoveConfig config;
    private UtilService utilService;
    public FileMoveTask(UtilService utilService, FileMoveConfig config,FileHelperService fileHelperService, ProcessItem processItem, MovingFileWorkerService movingFileWorkerService) {
        LOGGER.info("processing ");
        this.processItem = processItem;
        this.fileHelperService = fileHelperService;
        this.movingFileWorkerService = movingFileWorkerService;
        this.config = config;
        this.utilService = utilService;
    }

    @Override
    public void run() {
        int time = 0;
        FileHelperService.MoveShareResult result = null;
        String tempPath = config.tmpPath +"/"+utilService.pathOfMsisdn( processItem.getMisisdn())+"/"+processItem.getFileName();
        LOGGER.info("begin to move from : "+tempPath +" to : "+ config.destination);
        try {
            for(long retry: config.retries) {

                result = fileHelperService.moveToShare( tempPath, processItem.getFolder(), config.destination, processItem.getMisisdn());
                if( !result.isSuccess()) {
                    this.processItem.setStatus( 1 );
                }
                if (result.isSuccess()) {
                    break;
                }
                try {
                    long sleepTime = retry * 1000;
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                time++;
            }

        }catch (Exception ex){
            processItem.setDescription( ex.getMessage());
        }
        LOGGER.debug("done for file "+processItem.getFileName());
        String finalPath = "";
        if( result !=null) {
            finalPath = result.getFinalPath();
        }
        movingFileWorkerService.track( new FileMoveItem(processItem, time , new Date(), finalPath ));
    }

    public static class FileMoveConfig{
        private List<Long> retries;
        private String destination;
        private String tmpPath;

        public FileMoveConfig(List<Long> retries, String destination, String tmpPath) {
            this.retries = retries;
            this.destination = destination;

            this.tmpPath = tmpPath;
        }


    }
}
