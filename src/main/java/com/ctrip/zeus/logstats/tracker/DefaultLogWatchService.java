package com.ctrip.zeus.logstats.tracker;

import com.ctrip.zeus.logstats.common.FileChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2016/6/13.
 */
public class DefaultLogWatchService implements LogWatchService {
    private final String dir;
    private final Path dirPath;
    private final Set<String> registeredWatchingFiles;

    private WatchService watchService;
    private WatchKey watchKey;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultLogWatchService(String dir) {
        this.dir = dir;
        this.dirPath = Paths.get(dir);
        this.registeredWatchingFiles = new HashSet<>();
        init();
    }

    private boolean init() {
        try {
            if (watchService == null) {
                watchService = dirPath.getFileSystem().newWatchService();
            }
            if (watchKey == null) {
                watchKey = dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
            }
            return true;
        } catch (IOException e) {
            if (watchService == null) logger.error("Fail to create watch service to dir " + dir + ".", e);
            if (watchService != null && watchKey == null)
                logger.error("Fail to register events to watch service at dir " + dir + ".", e);
        }
        return false;
    }

    @Override
    public Path getWatchingPath() {
        return dirPath;
    }

    @Override
    public List<FileChangeEvent> pollEvents() {
        if (watchService == null || watchKey == null) {
            if (!init()) {
                return null;
            }
        }
        List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
        if (watchEvents.size() == 0) return null;

        List<FileChangeEvent> fileChangeEvents = new ArrayList<>();
        for (WatchEvent<?> watchEvent : watchEvents) {
            String event = watchEvent.kind().name();
            String filename = ((Path) watchEvent.context()).toString();
            logger.info("Log watch service signals event " + event + " on file " + filename + ".");
            if (registeredWatchingFiles.size() == 0) {
                fileChangeEvents.add(new FileChangeEvent(event, filename));
            } else if (registeredWatchingFiles.contains(filename)) {
                fileChangeEvents.add(new FileChangeEvent(event, filename));
            }
        }

        boolean valid = watchKey.reset();
        if (!valid) {
            logger.warn("Event monitor to dir " + dir + " is no longer available.");
            watchKey.cancel();
            watchKey = null;
            try {
                watchService.close();
                watchService = null;
            } catch (IOException e) {
                try {
                    watchService.close();
                    watchService = null;
                } catch (IOException e1) {
                    logger.error("Fail to close watch service of dir " + dir + ".");
                }
            }
        }

        return fileChangeEvents;
    }

    @Override
    public void registerWatchingFile(String simpleFilename) {
        registeredWatchingFiles.add(simpleFilename);
    }

    @Override
    public boolean avaiable() {
        return watchKey != null && watchService != null;
    }

    @Override
    public void start() {
        init();
    }

    @Override
    public void close() {
        try {
            watchService.close();
            watchService = null;
            watchKey = null;
        } catch (IOException e) {
            try {
                Thread.sleep(500L);
                try {
                    watchService.close();
                    watchService = null;
                    watchKey = null;
                } catch (IOException e1) {
                    logger.error("Fail to stop watch service of dir " + dir + ".", e1);
                }
            } catch (InterruptedException e1) {
            }
        }
    }
}
