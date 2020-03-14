package com.ctrip.zeus.client;

import com.ctrip.zeus.model.nginx.VirtualServerConfResponse;
import com.ctrip.zeus.nginx.LocalSlbConfResponse;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static com.ctrip.zeus.auth.util.AuthTokenUtil.getDefaultHeaders;

/**
 * @Discription
 **/
public class LocalNginxConfClient extends AbstractRestClient {

    private final Logger logger = LoggerFactory.getLogger(LocalNginxConfClient.class);

    public LocalNginxConfClient(String url, int timeout) {
        super(url, timeout);
    }

    /*
     * @Description fetch nginx.conf from server that specified by url
     * @return
     **/
    public String getNginxConf() {
        Response response = getTarget().path("/api/nginx/local/nginxconf").request().headers(getDefaultHeaders()).get();
        try {
            return IOUtils.inputStreamStringify((InputStream) response.getEntity());
        } catch (IOException e) {
            logger.warn("Could not stringify response entity. ");
        }
        return null;
    }

    public VirtualServerConfResponse getVsConf(Long vsId) {
        Response response = getTarget().path("/api/nginx/local/vsconf").queryParam("vsId", vsId).request().headers(getDefaultHeaders()).get();
        return parseResponseEntity(response, VirtualServerConfResponse.class);
    }

    public LocalSlbConfResponse getSlbConf() {
        Response response = getTarget().path("/api/nginx/local/slbconf").request().headers(getDefaultHeaders()).get();
        return parseResponseEntity(response, LocalSlbConfResponse.class);
    }

    private <T> T parseResponseEntity(Response response, Class<T> entityType) {
        if (response == null || entityType == null) {
            return null;
        }
        InputStream inputStream = (InputStream) response.getEntity();
        return ObjectJsonParser.parse(inputStream, entityType);
    }
}
