package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.FileMoveItem;

public interface MovingFileWorkerService {
    void start();
    void stop();
    void run();
    void track(FileMoveItem fileMoveItem);
    boolean isRetryQueueAvailable();

}
