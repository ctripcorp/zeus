package com.ctrip.zeus.service.tools.check;

public interface HealthCheckerStatusService {
    String getGroupStatus(String ip, String groupId);

    String getCheckerIP(String name);
}
