package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.FileMoveItem;
import com.asiacell.filemonitor.model.ProcessItem;

public interface MovingFileWorkerService {
    void start();
    void stop();
    void run();
    void track(FileMoveItem fileMoveItem);
    boolean isRetryQueueAvailable();
    void putInretry(ProcessItem processItem);

}
