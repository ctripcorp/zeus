package com.ctrip.zeus.nginx;

import com.ctrip.zeus.model.nginx.NginxResponse;

/**
 * Created by fanqq on 2015/6/25.
 */
public interface LocalValidate {
    public boolean pathExistValidate(String path , boolean isDirs)throws Exception;
    public NginxResponse nginxIsUp(String nginxBinPath)throws Exception;
}
