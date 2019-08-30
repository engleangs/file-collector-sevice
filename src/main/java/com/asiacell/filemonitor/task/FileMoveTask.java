package com.asiacell.filemonitor.task;

import com.asiacell.filemonitor.model.FileMoveItem;
import com.asiacell.filemonitor.model.ProcessItem;
import com.asiacell.filemonitor.service.MovingFileWorkerService;
import com.asiacell.filemonitor.service.util.FileHelperService;
import com.asiacell.filemonitor.service.util.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        this.processItem = processItem;
        this.fileHelperService = fileHelperService;
        this.movingFileWorkerService = movingFileWorkerService;
        this.config = config;
        this.utilService = utilService;
    }

    @Override
    public void run() {
        FileHelperService.MoveShareResult result = null;
        String tempPath = config.tmpPath +"/"+utilService.pathOfMsisdn( processItem.getMisisdn())+"/"+processItem.getFileName();
        LOGGER.info("begin to move from : "+tempPath +" to : "+ config.destination);
        processItem.setTempPath( tempPath );
        try {
            result = fileHelperService.moveToShare( tempPath, processItem.getFolder(), config.destination, processItem.getMisisdn() , processItem.getFileName());
        }catch (Exception ex){
            processItem.setDescription( ex.getMessage());
        }
        processItem.setDescription( result.getDescription());
            if (result.isSuccess()|| ( !result.isSuccess()
                    && this.processItem.getRetryTime() >= config.retries.size() )
                    || (!result.isSuccess()  && !movingFileWorkerService.isRetryQueueAvailable())) {
                this.processItem.setStatus(  result.isSuccess()?1:0);
                LOGGER.info("finish moving file to final directory : "+result.getFinalPath() +" retry :"+processItem.getRetryTime());
                movingFileWorkerService.track( new FileMoveItem(processItem, processItem.getRetryTime() , new Date(), result.getFinalPath(), processItem.getStartMoveFileDate(), new Date()));
            }
            else {
                int retry = config.retries.get( processItem.getRetryTime());

                Calendar calendar = Calendar.getInstance();
                calendar.add( Calendar.SECOND, retry);
                processItem.setExpectedRunDate( calendar.getTime() );
                processItem.retry();//keep retry
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                LOGGER.info( " -> sleep time "+ retry +" second and retry for "+processItem.getGuid()+","+processItem.getFileName() );
                LOGGER.info("  -> now " + simpleDateFormat.format( new Date()) +" to execute at "+simpleDateFormat.format( calendar.getTime()) );
            }


    }

    public static class FileMoveConfig{
        private List<Integer> retries;
        private String destination;
        private String tmpPath;

        public FileMoveConfig(List<Integer> retries, String destination, String tmpPath) {
            this.retries = retries;
            this.destination = destination;

            this.tmpPath = tmpPath;
        }


    }
}
