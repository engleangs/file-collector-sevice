package com.asiacell.filemonitor.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
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
    public MoveShareResult moveToShare(String filePath, String destination, String folder, String msisdn,String fileName) throws FileAlreadyExistsException {
            StringBuilder dest = new StringBuilder(destination);
            LOGGER.info("begin to move to share :" + filePath + ", to " + destination + " folder :" + folder + " with :" + msisdn);
            if (folder != null && !folder.isEmpty()) {
                dest.append("/").append(folder);
            }
            dest.append("/").append(utilService.pathOfMsisdn(msisdn));
            LOGGER.info("dest " + dest.toString());
        try {
                File destFolder = new File(dest.toString());
                LOGGER.info("destination folder "+dest.toString());
                if (!destFolder.exists()) {
                    boolean dirMakeResult = destFolder.mkdirs(); //for testing
                    LOGGER.info(" create new  directory result : "+dirMakeResult +" of "+dest.toString());
                }

                File file = new File(filePath);
                if (!file.exists()) {
                    new MoveShareResult(false, dest.toString(), "file_no_exist");
                }
                String newPath = dest.append("/").append( fileName).toString();
                LOGGER.info("new path = " + newPath);
                Files.copy(Paths.get(  filePath) , Paths.get( newPath));
                boolean deleteResult = file.delete();
                LOGGER.info("finish moving successfully & delete from local :"+deleteResult);

                MoveShareResult result = new MoveShareResult(true, newPath, "");
                return result;
        }catch (FileAlreadyExistsException ex){
            throw  ex;
        }
        catch (Exception ex){
            LOGGER.error("error during move file ",ex);
            return new MoveShareResult(false, dest.toString(), ex.getMessage());
        }


    }

}
