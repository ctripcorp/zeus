package com.ctrip.zeus.client;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by fanqq on 2015/4/28.
 */
public class LocalClient {
    private static final String LOCALHOST = "http://127.0.0.1";

    //TODO unify port get
    private static final DynamicIntProperty nginxDyupsPort = DynamicPropertyFactory.getInstance().getIntProperty("dyups.port", 8081);
    private static final DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);

    private static final DynamicIntProperty nginxDyupsTimeout = DynamicPropertyFactory.getInstance().getIntProperty("nginx.dyups.read.timeout", 500);

    private static final LocalClient localClient = new LocalClient();

    public static LocalClient getInstance() {
        return localClient;
    }

    private final NginxDyupsClient dyupsClient;
    private final NginxStatusClient statusClient;

    private UpstreamStatus upstreamStatus = null;

    public LocalClient() {
        dyupsClient = new NginxDyupsClient();
        statusClient = new NginxStatusClient();
    }

    protected LocalClient(String host) {
        dyupsClient = new NginxDyupsClient(host + ":" + nginxDyupsPort.get(), nginxDyupsTimeout.get());
        statusClient = new NginxStatusClient(host + ":" + nginxStatusPort.get());
    }

    public synchronized NginxResponse dyups(String upsName, String upsCommands) throws IOException {
        Response responseStr = dyupsClient.getTarget().path("/upstream/" + upsName).request().post(Entity.entity(upsCommands,
                MediaType.APPLICATION_JSON
        ));
        if (responseStr.getStatus() == 200) {
            return new NginxResponse().setSucceed(true).setOutMsg(responseStr.getEntity().toString());
        } else {
            return new NginxResponse().setSucceed(false).setErrMsg(responseStr.getEntity().toString());
        }
    }

    public UpstreamStatus getUpstreamStatus() throws IOException {
        String result = statusClient.getTarget().path("/status.json").request().get(String.class);
        upstreamStatus = DefaultJsonParser.parse(UpstreamStatus.class, result);
        return upstreamStatus;
    }

    public String getStubStatus() {
        return statusClient.getTarget().path("/stub_status").request().get(String.class);
    }

    public String getReqStatuses() {
        return statusClient.getTarget().path("/req_status").request().get(String.class);
    }

    private class NginxDyupsClient extends AbstractRestClient {
        public NginxDyupsClient() {
            this(LOCALHOST + ":" + nginxDyupsPort.get(), nginxDyupsTimeout.get());

        }

        protected NginxDyupsClient(String url, int timeout) {
            super(url, timeout);
        }
    }

    private class NginxStatusClient extends AbstractRestClient {
        public NginxStatusClient() {
            this(LOCALHOST + ":" + nginxStatusPort.get());
        }

        protected NginxStatusClient(String url) {
            super(url);
        }
    }
}
