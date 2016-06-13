package com.ctrip.zeus.logstats.analyzer;

import java.io.IOException;

/**
 * Created by mengyizhou on 10/18/15.
 */
public interface LogStatsAnalyzer {

    LogStatsAnalyzerConfig getConfig();

    void start() throws IOException;

    void stop() throws IOException;

    /**
     * This method check if the reading cursor has reached the end of the file at the time
     * when it is called. It might return a wrong value if file keeps growing.
     * This method is recommended to use iff reading a read-only file.
     * @return true if file cursor has reached the end of the file.
     * @throws IOException
     */
    boolean reachFileEnd() throws IOException;

    void run() throws IOException;

    String analyze() throws IOException;
}
