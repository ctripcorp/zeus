package com.ctrip.zeus.client;


import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.transform.DefaultJsonParser;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public class AppClient extends AbstractRestClient {
    public AppClient(String url) {
        super(url);
    }

    public List<App> getAll() {
        String res = getTarget().path("/api/app").request().headers(getDefaultHeaders()).get(String.class);
        return null;
    }

    public Response add(App app) {
        return getTarget().path("/api/app/add").request().headers(getDefaultHeaders())
                .post(Entity.entity(
                        String.format(App.JSON, app),
                        MediaType.APPLICATION_JSON
                ));

    }

    public App get(String appName) {
        String res = getTarget().path("/api/app/get/" + appName).request(MediaType.APPLICATION_JSON).headers(getDefaultHeaders()).get(String.class);
        try {
            return DefaultJsonParser.parse(App.class, res);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
