package com.ctrip.zeus.logstats.parser.state;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/7.
 */
public interface StateContext<V> {

    void setSourceValue(V value);

    int getCurrentIndex();

    int getStateHistoryCount();

    void proceed(int length);

    char[] delay(int length);

    char[] getSource();

    void addResult(V result);

    String getLastStateValue();

    List<String> getParsedValue();

    boolean shouldProceed();
}
