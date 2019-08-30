package com.asiacell.filemonitor.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
@Service
public class FileHelperServiceImpl implements FileHelperService {
    private static Logger LOGGER = LoggerFactory.getLogger(FileHelperService.class);
    @Autowired
    private UtilService utilService;

    @Override
    public boolean moveTobyDate(String fileName, Path source, Path dest) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date now = new Date();
        String today = simpleDateFormat.format( now );
        File file = new File(source.toString()+"/"+fileName);
        File dir = new File(dest.toString()+"/"+ today);
        if( !dir.exists() ) {
            dir.mkdirs();
        }
        File destFile = new File(dest.toString()+"/"+today+"/"+fileName);
        return file.renameTo( destFile);
    }

    @Override
    public MoveShareResult moveToShare(String filePath, String destination, String folder, String msisdn,String fileName) {
        Path dest ;
        LOGGER.debug("begin to move to share :"+filePath+", to "+destination +" folder :"+folder +" with :"+msisdn);
        if(folder !=null && !folder.isEmpty()) {
            dest  = Paths.get( destination,folder, utilService.pathOfMsisdn(msisdn));
        }
        else {
            dest = Paths.get( destination , utilService.pathOfMsisdn( msisdn));
        }
        File destFolder  = new File( dest.toString());
        if( !destFolder.exists()) {
            destFolder.mkdirs(); //for testing
        }

        File file = new File( filePath);
        if( !file.exists()) {
            new MoveShareResult(false,dest.toString(), "file_no_exist");
        }
        String newPath = dest.toString() +"/"+fileName;
        LOGGER.debug("new path = "+ newPath);
        boolean rename = file.renameTo(new File( newPath) );
        MoveShareResult result = new MoveShareResult( rename, newPath,"");
        if(!rename) {
            result.setDescription( "exist_move_fail");
        }
        return result;

    }

}
