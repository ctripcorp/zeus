package com.ctrip.zeus.service.query;

import com.ctrip.zeus.exceptions.ValidationException;

/**
 * Created by zhoumy on 2016/7/15.
 */
public interface QueryCommand {

    boolean add(String queryName, String queryValue) throws ValidationException;

    boolean addAtIndex(int idx, String queryValue) throws ValidationException;

    boolean hasValue(int idx);

    String[] getValue(int idx);

    String getType();
}
