package com.ctrip.zeus.logstats.parser.state;

import java.util.LinkedList;

/**
 * Created by zhoumy on 2016/6/7.
 */
public interface StateMachineContext<R> {

    void setSourceValue(String value);

    void proceed(int length);

    char[] delay(int length);

    char[] getSource();

    int getCurrentIndex();

    boolean shouldProceed();

    void addResult(String key, String value);

    String peekLastParsedValue();

    LinkedList<R> getResult();

}
