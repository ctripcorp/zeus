package com.ctrip.zeus.service.model;

import java.util.List;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface PathRewriteParser {

    boolean validate(String value);

    List<String> getValues(String value);
}
