package com.ctrip.zeus.client;

import com.ctrip.zeus.nginx.entity.NginxResponse;
import com.ctrip.zeus.nginx.entity.ReqStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.nginx.entity.UpstreamStatus;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;
import com.ctrip.zeus.util.S;
import com.google.common.base.Preconditions;
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
    private static final DynamicIntProperty nginxDyupsPort = DynamicPropertyFactory.getInstance().getIntProperty("dyups.port", 8081);
    private static final DynamicIntProperty nginxStatusPort = DynamicPropertyFactory.getInstance().getIntProperty("slb.nginx.status-port", 10001);
    private static final LocalClient localClient = new LocalClient();

    private final NginxDyupsClient dyupsClient;
    private final NginxStatusClient statusClient;

    public LocalClient() {
        dyupsClient = new NginxDyupsClient();
        statusClient = new NginxStatusClient();
    }

    public LocalClient(String host) {
        dyupsClient = new NginxDyupsClient(host + ":" + nginxDyupsPort.get());
        statusClient = new NginxStatusClient(host + ":" + nginxStatusPort.get());
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

    public TrafficStatus getTrafficStatus() {
        TrafficStatus trafficStatus = new TrafficStatus();
        trafficStatus.setServerIp(S.getIp());
        getStubStatus(trafficStatus);
        getReqStatuses(trafficStatus);
        return trafficStatus;
    }

    private void getStubStatus(TrafficStatus trafficStatus) {
        String response = statusClient.getTarget().path("/stub_status").request().get(String.class);
        extractStubStatus(response.split("\n"), trafficStatus);
    }

    private void getReqStatuses(TrafficStatus trafficStatus) {
        String response = statusClient.getTarget().path("/req_status").request().get(String.class);
        String[] entites = response.split("\n");
        for (String en : entites) {
            trafficStatus.addReqStatus(extractReqStatus(en.split(",")));
        }
    }

    private static ReqStatus extractReqStatus(String[] values) {
        Preconditions.checkState(values != null && values.length == ReqStatusOffset.values().length + 1);

        String[] hostUpstream = values[0].split("/");
        String hostName, upstreamName;
        hostName = upstreamName = "";
        if (hostUpstream.length > 0) {
            hostName = hostUpstream[0];
            if (hostUpstream.length > 1)
                upstreamName = hostUpstream[1];
        }

        Integer[] data = new Integer[values.length - 1];
        for (int i = 0; i < data.length; i++) {
            data[i] = Integer.parseInt(values[i + 1]);
        }
        return new ReqStatus().setHostName(hostName)
                .setTotalRequests(data[ReqStatusOffset.ReqTotal.ordinal()])
                .setUpName(upstreamName)
                .setUpRequests(data[ReqStatusOffset.UpstreamReq.ordinal()])
                .setUpResponseTime(data[ReqStatusOffset.UpstreamRt.ordinal()])
                .setUpTries(data[ReqStatusOffset.UpstreamTries.ordinal()])
                .setSuccessCount(data[ReqStatusOffset.SuccessCount.ordinal()])
                .setRedirectionCount(data[ReqStatusOffset.RedirectionCount.ordinal()])
                .setClientErrCount(data[ReqStatusOffset.ClientErrCount.ordinal()])
                .setServerErrCount(data[ReqStatusOffset.ServerErrorCount.ordinal()]);
    }

    private static void extractStubStatus(String[] values, TrafficStatus trafficStatus) {
        Preconditions.checkState(values.length == 4);
        final String activeConnectionKey = "Active connections: ";
        final String readingKey = "Reading: ";
        final String writingKey = "Writing: ";
        final String waitingKey = "Waiting: ";
        Integer[] data = new Integer[StubStatusOffset.values().length];
        data[0] = Integer.parseInt(values[0].trim().substring(activeConnectionKey.length()));

        String[] reqSrc = values[2].trim().split(" ");
        for (int i = 0; i < reqSrc.length; i++) {
            data[i + 1] = Integer.parseInt(reqSrc[i]);
        }
        String stateSrc = values[3].trim();
        data[5] = Integer.parseInt(stateSrc.substring(readingKey.length(), stateSrc.indexOf(writingKey) - 1));
        data[6] = Integer.parseInt(stateSrc.substring(stateSrc.indexOf(writingKey) + writingKey.length(), stateSrc.indexOf(waitingKey) - 1));
        data[7] = Integer.parseInt(stateSrc.substring(stateSrc.indexOf(waitingKey) + waitingKey.length()));

        trafficStatus.setActiveConnections(data[StubStatusOffset.ActiveConn.ordinal()])
                .setAccepts(data[StubStatusOffset.Accepts.ordinal()])
                .setHandled(data[StubStatusOffset.Handled.ordinal()])
                .setRequests(data[StubStatusOffset.Requests.ordinal()])
                .setRequestTime(data[StubStatusOffset.RequestTime.ordinal()])
                .setReading(data[StubStatusOffset.Reading.ordinal()])
                .setWriting(data[StubStatusOffset.Writing.ordinal()])
                .setWaiting(data[StubStatusOffset.Waiting.ordinal()]);
    }

    private enum StubStatusOffset {
        ActiveConn, Accepts, Handled, Requests, RequestTime, Reading, Writing, Waiting
    }

    private enum ReqStatusOffset {
        BytInTotal, BytOutTotal, ConnTotal, ReqTotal, SuccessCount, RedirectionCount,
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
