package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.service.model.PathRewriteParser;
import org.springframework.stereotype.Component;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("pathRewriteParser")
public class DefaultRewriteParser implements PathRewriteParser {
    @Override
    public void validate() {

    }

    @Override
    public String[] getValues() {
        return new String[0];
    }
}
