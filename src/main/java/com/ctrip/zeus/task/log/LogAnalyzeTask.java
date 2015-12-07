package com.ctrip.zeus.task.log;

import com.ctrip.zeus.task.AbstractTask;
import org.springframework.stereotype.Component;

/**
 * @author:xingchaowang
 * @date: 12/7/2015.
 */
@Component("logAnalyzeTask")
public class LogAnalyzeTask extends AbstractTask {
    private int i = 0;

    @Override
    public void start() {

    }

    @Override
    public long getInterval() {
        return 2000;
    }

    @Override
    public void run() {
        System.out.println("########################################" + i++);
    }

    @Override
    public void stop() {

    }
}
