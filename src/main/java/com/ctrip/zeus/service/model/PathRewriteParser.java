package com.ctrip.zeus.service.model;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface PathRewriteParser {
    void validate();

    String[] getValues();
}
