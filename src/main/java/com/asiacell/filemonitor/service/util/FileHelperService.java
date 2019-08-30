package com.asiacell.filemonitor.service.util;

import java.nio.file.Path;

public interface FileHelperService {
    boolean moveTobyDate(String fileName, Path source, Path dest);


    MoveShareResult moveToShare(String filePath, String destination, String folder, String msisdn);
    class MoveShareResult{
        private boolean success;
        private String finalPath;
        private String description;

        public MoveShareResult(boolean success, String finalPath, String description) {
            this.success = success;

            this.finalPath = finalPath;
            this.description = description;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getFinalPath() {
            return finalPath;
        }

        public void setFinalPath(String finalPath) {
            this.finalPath = finalPath;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
