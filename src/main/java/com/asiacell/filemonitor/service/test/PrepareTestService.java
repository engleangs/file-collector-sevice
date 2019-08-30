package com.asiacell.filemonitor.service.test;

import com.asiacell.filemonitor.model.WorkerItem;
import com.asiacell.filemonitor.service.util.UtilService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PrepareTestService {
    @Autowired
    UtilService utilService;
    Gson gson = new Gson();
    public void addData(TestItem testItem,String type ,String fileName) {
        if("D1".equals( type)) {
            testItem.setDoc1( fileName);
        }
        else if("D2".equals( type)) {
            testItem.setDoc2( fileName );
        }
        else if ( "PH".equals(type)) {
            testItem.setPhoto( fileName);
        }
        else if("FP".equals( type )) {
            testItem.setFingerprint( fileName);
        }
        else if("SG".equals( type)) {
            testItem.setSignature( fileName);
        }
    }

    public void moveToDest(File file, String fileName,String msisdn, String destPath) throws IOException {
        String segmentPath = utilService.pathOfMsisdn( msisdn);
        File destFolder = new File( destPath+"/"+ segmentPath);
        if(!destFolder.exists()) {
            destFolder.mkdirs();
        }
        Files.copy( Paths.get(file.getPath()), Paths.get(destPath, segmentPath, fileName));
    }

    public Map<String,TestItem> prepareData(String basePath,String destPath) throws IOException {
        Map<String,TestItem> data  = new HashMap<>();
        File file = new File( basePath);
        for(File imgFile : file.listFiles() ){
            String fileName = imgFile.getName();
            String[] arrName = fileName.split("_");
            String msisdn = arrName[0];
            String type = arrName[1];
            if( data.containsKey( msisdn)) {
                addData( data.get( msisdn), type, fileName);
            }
            else {
                TestItem testItem = new TestItem();
                testItem.setMsisdn( msisdn);
                addData( testItem , type, fileName);
                data.put( msisdn , testItem);
            }
            moveToDest( imgFile, fileName,msisdn, destPath);

        }

        return data;

    }

    public void  generateTest(Map<String,TestItem>data,String dataPath) throws IOException {
        for(String key : data.keySet()) {
            TestItem testItem = data.get( key);
            List<String>file  = Arrays.asList( testItem.photo, testItem.doc1, testItem.doc2,testItem.fingerprint ,testItem.signature);
            WorkerItem workerItem = new WorkerItem( testItem.msisdn , "",file,null);
            String json = gson.toJson( workerItem);
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(dataPath + "/" + testItem.msisdn + ".txt"));
                writer.write(json);
                writer.flush();
                writer.close();

            }catch (Exception ex){
            ex.printStackTrace();
            }

        }
    }


    public void prepareTestData(String testSourcePath,String tmpPath,String dataPath) throws IOException {
        Map<String,TestItem>data = prepareData( testSourcePath,tmpPath);
        generateTest( data, dataPath);
    }

    public static class TestItem{
        private String msisdn;
        private String photo;
        private String fingerprint;
        private String doc1;
        private String doc2;
        private String signature;
        public TestItem(){

        }

        public TestItem(String msisdn, String photo, String fingerprint, String doc1, String doc2, String signature) {
            this.msisdn = msisdn;
            this.photo = photo;
            this.fingerprint = fingerprint;
            this.doc1 = doc1;
            this.doc2 = doc2;
            this.signature = signature;
        }

        public String getMsisdn() {
            return msisdn;
        }

        public void setMsisdn(String msisdn) {
            this.msisdn = msisdn;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }

        public String getDoc1() {
            return doc1;
        }

        public void setDoc1(String doc1) {
            this.doc1 = doc1;
        }

        public String getDoc2() {
            return doc2;
        }

        public void setDoc2(String doc2) {
            this.doc2 = doc2;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }
}
