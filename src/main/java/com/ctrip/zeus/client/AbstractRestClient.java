package com.ctrip.zeus.client;

import com.ctrip.zeus.auth.impl.IPAuthenticationFilter;
import com.ctrip.zeus.auth.impl.TokenManager;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.uri.UriComponent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

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

    protected MultivaluedMap<String, Object> getDefaultHeaders(){
        MultivaluedMap<String, Object> map = new MultivaluedHashMap<>();
        map.putSingle(IPAuthenticationFilter.SERVER_TOKEN_HEADER, TokenManager.generateToken());
        return map;
    }
}
