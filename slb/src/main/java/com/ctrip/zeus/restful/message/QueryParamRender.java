package com.ctrip.zeus.restful.message;

import javax.ws.rs.core.UriInfo;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by zhoumy on 2016/7/19.
 */
public class QueryParamRender {
    public static Queue<String[]> extractRawQueryParam(UriInfo uriInfo) {
        Queue<String[]> params = new LinkedList<>();
        for (Map.Entry<String, List<String>> e : uriInfo.getQueryParameters().entrySet()) {
            if (e.getValue() != null && e.getValue().size() > 0) {
                for (String v : e.getValue()) {
                    params.add(new String[]{e.getKey(), v.trim()});
                }
            }
        }
        return params;
    }
}
