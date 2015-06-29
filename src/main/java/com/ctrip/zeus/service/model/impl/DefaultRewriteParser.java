package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.service.model.handler.RewriteParseHandler;
import com.ctrip.zeus.service.model.handler.impl.ParseException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/6/29.
 */
@Component("pathRewriteParser")
public class DefaultRewriteParser implements PathRewriteParser {
    @Resource
    private RewriteParseHandler rewriteParseHandler;

    @Override
    public boolean validate(String value) {
        return rewriteParseHandler.validate(value.getBytes());
    }

    @Override
    public List<String> getValues(String value) {
        List<String> output = new ArrayList<>();
        List<String> result = new ArrayList<>();
        try {
            rewriteParseHandler.handleContent(value.getBytes(), output);
        } catch (ParseException e) {
            return null;
        }
        String oneRecord = "";
        for (int i = 0; i < output.size(); i++) {
            if (i % 2 == 0)
                oneRecord = "\"" + output.get(i) + "\"";
            else {
                oneRecord += " " + output.get(i) + ";";
                result.add(oneRecord);
            }
        }
        return result;
    }
}
