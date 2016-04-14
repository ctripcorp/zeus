package com.ctrip.zeus.client;

import com.ctrip.zeus.auth.impl.IPAuthenticationFilter;
import com.ctrip.zeus.auth.impl.TokenManager;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.uri.UriComponent;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public abstract class AbstractRestClient {

    private WebTarget webTarget;

    private static DynamicIntProperty connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty("client.connect.timeout", 1000);
    private static DynamicIntProperty readTimeout = DynamicPropertyFactory.getInstance().getIntProperty("client.read.timeout", 30000);

    protected AbstractRestClient(String url) {
        Client client = ClientBuilder.newBuilder()
                .withConfig(new ClientConfig())
                .register(MultiPartFeature.class)
                .build();
        client.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout.get());
        client.property(ClientProperties.READ_TIMEOUT, readTimeout.get());
        webTarget = client.target(url);
    }

    protected AbstractRestClient(String url, int readTimeout) {
        Client client = ClientBuilder.newBuilder()
                .withConfig(new ClientConfig())
                .register(MultiPartFeature.class)
                .build();
        client.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout.get());
        client.property(ClientProperties.READ_TIMEOUT, readTimeout);
        webTarget = client.target(url);
    }

    public String urlEncode(String url) {
        return UriComponent.encode(url, UriComponent.Type.PATH_SEGMENT);
    }

    protected WebTarget getTarget() {
        return webTarget;
    }

    protected MultivaluedMap<String, Object> getDefaultHeaders() {
        MultivaluedMap<String, Object> map = new MultivaluedHashMap<>();
        map.putSingle(IPAuthenticationFilter.SERVER_TOKEN_HEADER, TokenManager.generateToken());
        return map;
    }
}
