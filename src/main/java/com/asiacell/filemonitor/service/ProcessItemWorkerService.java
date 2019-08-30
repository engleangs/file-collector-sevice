package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.WorkerItem;

public interface ProcessItemWorkerService {
    void start();
    void stop();
    void run();
    int size();
    void add(WorkerItem workerItem);
}
