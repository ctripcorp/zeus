package com.ctrip.zeus.service.model;

import com.ctrip.zeus.service.model.grammar.RewriteParseHandler;
import com.ctrip.zeus.service.model.grammar.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2015/6/29.
 */
public class PathRewriteParser {

    public static boolean validate(String value) {
        return new RewriteParseHandler().validate(value.getBytes());
    }

    public static List<String> getValues(String value) {
        List<String> output = new ArrayList<>();
        List<String> result = new ArrayList<>();
        try {
            new RewriteParseHandler().handleContent(value.getBytes(), output);
        } catch (ParseException e) {
            return result;
        }
        String oneRecord = "";
        for (int i = 0; i < output.size(); i++) {
            if (i % 2 == 0)
                oneRecord = "\"" + output.get(i) + "\"";
            else {
                oneRecord += " " + output.get(i);
                result.add(oneRecord);
            }
        }
        return result;
    }
}
