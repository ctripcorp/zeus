package com.ctrip.zeus.restful.resource.meta;

import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.support.ObjectJsonWriter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 2016/8/11.
 */
public abstract class MetaSearchResource {
    @Resource
    private ResponseHandler responseHandler;

    abstract  MetaDataCache<List<CacheItem>> getCache();

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response search(@Context final HttpHeaders hh,
                           @Context HttpServletRequest request,
                           @QueryParam("q") String q,
                           @DefaultValue("20") @QueryParam("limit") int limit) throws Exception {
        List<CacheItem> all = getCache().get();

        List<CacheItem> res = new ArrayList<>(limit);
        int count = 0;
        for (CacheItem s : all) {
            if (q != null && !(s.toString().toLowerCase().startsWith(q.toLowerCase()))) {
                continue;
            }
            res.add(s);
            count++;
            if (count == limit) break;
        }
        if(count < limit) {
            for (CacheItem s : all) {
                if (q != null && s.toString().toLowerCase().indexOf(q.toLowerCase()) < 0) {
                    continue;
                }
                if (!res.contains(s)) {
                    res.add(s);
                    count++;
                    if (count == limit) break;
                }
            }
        }
        return responseHandler.handleSerializedValue(ObjectJsonWriter.write(res), hh.getMediaType());
    }
}
