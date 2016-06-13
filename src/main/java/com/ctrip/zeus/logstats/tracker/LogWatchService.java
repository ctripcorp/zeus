package com.ctrip.zeus.logstats.tracker;

import com.ctrip.zeus.logstats.common.FileChangeEvent;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by zhoumy on 2016/6/13.
 */
public interface LogWatchService {

    Path getWatchingPath();

    List<FileChangeEvent> pollEvents();

    void registerWatchingFile(String simpleFilename);

    boolean avaiable();

    void close();

    String CREATE_EVENT = "ENTRY_CREATE";
    String DELETE_EVENT = "ENTRY_DELETE";
}
