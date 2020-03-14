package com.ctrip.zeus.client;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.uri.UriComponent;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 * @author:xingchaowang
 * @date: 3/6/2015.
 */
public abstract class AbstractRestClient {

    private final String baseUrl;
    private WebTarget webTarget;
    private Client client;

    private static DynamicIntProperty connectTimeout = DynamicPropertyFactory.getInstance().getIntProperty("client.connect.timeout", 1000);
    private static DynamicIntProperty readTimeout = DynamicPropertyFactory.getInstance().getIntProperty("client.read.timeout", 60000);

    protected AbstractRestClient(String url) {
        client = ClientBuilder.newBuilder()
                .withConfig(new ClientConfig())
                .register(MultiPartFeature.class)
                .build();
        client.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout.get());
        client.property(ClientProperties.READ_TIMEOUT, readTimeout.get());
        webTarget = client.target(url);
        baseUrl = url;
    }

    protected AbstractRestClient(String url, int readTimeout, SSLContext sslContext, HostnameVerifier hostnameVerifier) {
        Client client = ClientBuilder.newBuilder()
                .sslContext(sslContext)
                .hostnameVerifier(hostnameVerifier)
                .withConfig(new ClientConfig())
                .register(MultiPartFeature.class)
                .build();
        client.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout.get());
        client.property(ClientProperties.READ_TIMEOUT, readTimeout);
        webTarget = client.target(url);
        baseUrl = url;
    }

    protected AbstractRestClient(String url, int readTimeout) {
        client = ClientBuilder.newBuilder()
                .withConfig(new ClientConfig())
                .register(MultiPartFeature.class)
                .build();
        client.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout.get());
        client.property(ClientProperties.READ_TIMEOUT, readTimeout);
        webTarget = client.target(url);
        baseUrl = url;
    }

    public void updateUrl(String url) {
        this.webTarget = this.client.target(url);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String urlEncode(String url) {
        return UriComponent.encode(url, UriComponent.Type.PATH_SEGMENT);
    }

    protected WebTarget getTarget() {
        return webTarget;
    }


}
