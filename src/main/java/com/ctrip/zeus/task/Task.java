package com.ctrip.zeus.task;

/**
 * @author:xingchaowang
 * @date: 12/7/2015.
 */
public interface Task {

    public void run();
    public String getName();
    public long getInterval();
}
