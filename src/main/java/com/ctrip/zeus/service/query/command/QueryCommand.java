package com.ctrip.zeus.service.query.command;

/**
 * Created by zhoumy on 2016/7/15.
 */
public interface QueryCommand {

    boolean add(String queryName, String queryValue);

    boolean addAtIndex(int idx, String queryValue);

    boolean hasValue(int idx);

    String[] getValue(int idx);

    String getType();
}
