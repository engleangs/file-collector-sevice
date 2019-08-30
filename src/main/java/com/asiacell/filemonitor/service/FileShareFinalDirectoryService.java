package com.asiacell.filemonitor.service;

import java.io.IOException;

public interface FileShareFinalDirectoryService {
    void start() throws IOException;
    void stop();
    void run();
    void size();
}
