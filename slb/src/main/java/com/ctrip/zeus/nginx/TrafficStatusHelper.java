package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.nginx.ReqStatus;

/**
 * Created by zhoumy on 2015/6/25.
 */
public class TrafficStatusHelper {
    public static ReqStatus add(ReqStatus origin, ReqStatus delta, String hostname, Long slbId, Long groupId, String groupName) {
        if (origin == null)
            return delta.setHostName(hostname).setSlbId(slbId)
                    .setGroupName(groupName).setGroupId(groupId);
        return new ReqStatus().setHostName(hostname).setSlbId(slbId)
                .setGroupName(groupName).setGroupId(groupId)
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
