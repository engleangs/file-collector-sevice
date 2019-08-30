package com.asiacell.filemonitor.service.data;

import com.asiacell.filemonitor.model.FileMoveItem;
import com.asiacell.filemonitor.model.ProcessItem;

import java.util.List;
import java.util.Set;

public interface DataDaoService {
    List<ProcessItem> getListItem();
    int addBatchFileItems(List<FileMoveItem>fileMoveItems);
    int addBatchProcessItems(List<ProcessItem>processItems);
    int checkFileNotFoundDb(String hostname,ProcessItem processItem);
    void trackFileNotFound(String hostname,ProcessItem processItem);
    void trackOwnService();
    Set<String> getAllServiceHost();
    List<String>getAllHostForFileNotFound(ProcessItem processItem);
    int moveToNotFound(ProcessItem processItem);

}
