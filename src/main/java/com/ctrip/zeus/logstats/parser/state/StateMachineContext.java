package com.ctrip.zeus.logstats.parser.state;

import com.ctrip.zeus.logstats.parser.KeyValue;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/7.
 */
public interface StateMachineContext {

    void setSourceValue(String value);

    void proceed(int length);

    char[] delay(int length);

    char[] getSource();

    int getCurrentIndex();

    boolean shouldProceed();

    void addResult(String key, String value);

    String getLastParsedValue();

    List<KeyValue> getResult();

}
