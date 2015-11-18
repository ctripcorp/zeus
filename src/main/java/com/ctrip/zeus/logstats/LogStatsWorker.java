package com.ctrip.zeus.logstats;

import java.io.IOException;

/**
 * Created by zhoumy on 2015/11/17.
 */
public interface LogStatsWorker {
    
    void doJob() throws IOException;
}
