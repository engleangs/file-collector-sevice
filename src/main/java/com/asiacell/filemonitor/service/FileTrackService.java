package com.asiacell.filemonitor.service;

import com.asiacell.filemonitor.model.FileMonitorItem;

public interface FileTrackService {
    void add(FileMonitorItem item);
    void start();
    void stop();
    int size();
}
