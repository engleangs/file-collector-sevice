package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.ProcessItem;

public interface ProcessItemQueueService {
    void start();
    void stop();
    void run();
    void add(ProcessItem item);
}
