package com.ctrip.zeus.service.build;

import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.service.model.snapshot.ModelSnapshotEntity;


/**
 * @Discription Conf builder class for building conf(nginx, vhosts, upstream) out of ModelSnapshotEntity
 **/
public interface ConfSnapshotBuildService {

    String buildNginxConf(ModelSnapshotEntity snapshot) throws Exception;

    NginxConfEntry buildFullConfEntry(ModelSnapshotEntity snapshot) throws Exception;

    NginxConfEntry buildIncrementalEntry(ModelSnapshotEntity snapshotEntity) throws Exception;
}
