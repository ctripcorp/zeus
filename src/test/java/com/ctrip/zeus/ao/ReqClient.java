package com.ctrip.zeus.ao;

import com.ctrip.zeus.client.AbstractRestClient;
import org.glassfish.jersey.client.ClientConfig;
import test.StringDemo;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by fanqq on 2015/3/30.
 */
public class ReqClient {

    private String host = null;
    private Client client = null;
    public static ReqClient reqClient = new ReqClient("");

    public ReqClient(String host) {
        ClientConfig config = new ClientConfig();
        client = ClientBuilder.newClient(config);
        this.host = host;
    }

    public void setHost(String host){
        this.host = host;
    }
    public String getHost(){
        return  host;
    }

    private WebTarget getTarget(){
        return client.target(host);
    }
    private WebTarget getTarget(String path)
    {
        return client.target(host+path);
    }

    public Response post(String path, String data) {
        Response res = getTarget().path(path).request()
                .post(Entity.entity(data,
                        MediaType.APPLICATION_JSON
                ));
        return res;
    }

    public Response get() {
        Response res = getTarget().request()
                .get();

        return res;
    }
    public Response get(String path )
    {
        return getTarget(path).request().get();
    }

    public String getstr() {
        String res = getTarget().request()
                .get(String.class);
        return res;
    }

    public String getstr(String path) {
        return getTarget(path).request().get(String.class);
    }

}
