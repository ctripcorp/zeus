package com.ctrip.zeus.service.healthcheck;

import com.ctrip.zeus.domain.ClusterInfo;
import com.ctrip.zeus.exceptions.ValidationException;

import java.util.List;
import java.util.Set;

public interface HealthCheckService {
    /**
     * Register Health Checker Member.
     * Member Should Register At Least 1 Time Per Hour. Otherwise This Member Will Be Disabled.
     * @param ip
     * @param cluster
     * @return
     */
    String register(String ip, String cluster) throws ValidationException;

    /***
     * Get Properties For Special Cluster.
     * @param cluster
     * @return
     */
    String properties(String cluster);

    /***
     * Get SlbIds Should Be Checked By Special Cluster.
     * @param cluster
     * @return
     */
    Set<Long> checkSlbIds(String cluster) throws Exception;

    /**
     * Remove Disabled Members.
     * @throws Exception
     */
    void cleanDisabledMembers() throws Exception;

    List<ClusterInfo> list() throws Exception;

    ClusterInfo getClusterInfoByName(String name) throws Exception;

    void updateProperties(ClusterInfo info) throws Exception;
}
