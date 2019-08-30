package com.asiacell.filemonitor.service;

import java.io.IOException;

public interface WatcherService {
    void start() throws IOException;
    void stop() throws IOException;
    void run() throws IOException, InterruptedException;
    int size();
}
