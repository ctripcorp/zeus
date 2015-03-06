package com.ctrip.zeus.client;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.uri.UriComponent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public abstract class AbstractRestClient {

    private WebTarget webTarget;

    protected AbstractRestClient(String url) {
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        webTarget = client.target(url);
    }

    public String urlEncode(String url) {
        return UriComponent.encode(url, UriComponent.Type.PATH_SEGMENT);
    }

    protected WebTarget getTarget(){
        return webTarget;
    }
}
