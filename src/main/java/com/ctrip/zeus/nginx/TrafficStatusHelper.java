package com.ctrip.zeus.nginx;

import com.ctrip.zeus.nginx.entity.ReqStatus;

/**
 * Created by zhoumy on 2015/6/25.
 */
public class TrafficStatusHelper {
    public static ReqStatus add(ReqStatus origin, ReqStatus delta, String groupName, String hostname) {
        if (origin == null)
            return delta.setGroupName(groupName).setHostName(hostname);
        return new ReqStatus().setHostName(hostname).setGroupName(groupName)
                .setBytesInTotal(origin.getBytesInTotal() + delta.getBytesInTotal())
                .setBytesOutTotal(origin.getBytesOutTotal() + delta.getBytesOutTotal())
                .setSuccessCount(origin.getSuccessCount() + delta.getSuccessCount())
                .setRedirectionCount(origin.getRedirectionCount() + delta.getRedirectionCount())
                .setClientErrCount(origin.getClientErrCount() + delta.getClientErrCount())
                .setServerErrCount(origin.getServerErrCount() + delta.getServerErrCount())
                .setResponseTime(origin.getResponseTime() + delta.getResponseTime())
                .setTotalRequests(origin.getTotalRequests() + delta.getTotalRequests())
                .setUpRequests(origin.getUpRequests() + delta.getUpRequests())
                .setUpResponseTime(origin.getUpResponseTime() + delta.getUpResponseTime())
                .setUpTries(origin.getUpTries() + delta.getUpTries())
                .setTime(origin.getTime());
    }
}
