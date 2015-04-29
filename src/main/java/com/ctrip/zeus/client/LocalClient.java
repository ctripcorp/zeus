package com.ctrip.zeus.client;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;
import com.google.common.base.Preconditions;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2015/4/28.
 */
public class LocalClient {
    private static final String LOCALHOST = "127.0.0.1";
    private static final DynamicIntProperty nginxDyupsPort = DynamicPropertyFactory.getInstance().getIntProperty("dyups.port", 8081);
    private static final DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);
    private static final LocalClient localClient = new LocalClient();

    private final NginxDyupsClient dyupsClient;
    private final NginxStatusClient statusClient;

    public LocalClient() {
        dyupsClient = new NginxDyupsClient();
        statusClient = new NginxStatusClient();
    }

    public static LocalClient getInstance() {
        return localClient;
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
        System.out.println(result);
        return DefaultJsonParser.parse(UpstreamStatus.class, result);
    }

    public List<TrafficStatus> getTrafficStatus() {
        String response = statusClient.getTarget().path("/metrics").request().get(String.class);
        String[] entites = response.split("\n");
        List<TrafficStatus> list = new ArrayList<>();
        for (String en : entites)
            list.add(toTrafficStatus(en.split(",")));
        return list;
    }

    private static TrafficStatus toTrafficStatus(String[] values) {
        int[] data = new int[values.length - 1];
        for (int i = 1; i < values.length; i++) {
            data[i - 1] = Integer.parseInt(values[i]);
        }
        Preconditions.checkState(values.length == Offset.values().length);
        int avgResponseTime = data[Offset.UpstreamReq.ordinal()] == 0 ? 0 : data[Offset.UpstreamRt.ordinal()] / data[Offset.UpstreamReq.ordinal()];
        return new TrafficStatus().setAvgResponseTime(avgResponseTime)
                .setBytin(data[Offset.BytInTotal.ordinal()]).setBytout(data[Offset.BytOutTotal.ordinal()])
                .setHostName(values[Offset.Hostname.ordinal()])
                .setSuccessCount(data[Offset.SuccesCount.ordinal()])
                .setRedirectionCount(data[Offset.RedirectionCount.ordinal()])
                .setClientErrCount(data[Offset.ClientErrCount.ordinal()])
                .setServerErrCount(data[Offset.ServerErrorCount.ordinal()]);
    }

    private enum Offset {
        Hostname, BytInTotal, BytOutTotal, ConnTotal, ReqTotal, SuccesCount, RedirectionCount,
        ClientErrCount, ServerErrorCount, Other, RtTotal, UpstreamReq, UpstreamRt, UpstreamTries
    }

    private class NginxDyupsClient extends AbstractRestClient {
        public NginxDyupsClient() {
            this(LOCALHOST + ":" + nginxDyupsPort.get());
        }

        protected NginxDyupsClient(String url) {
            super(url);
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
