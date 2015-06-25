package com.ctrip.zeus.nginx;

/**
 * Created by fanqq on 2015/6/25.
 */
public interface LocalValidate {
    public boolean pathExistValidate(String path , boolean isDirs)throws Exception;
    public boolean nginxIsUp(String nginxBinPath)throws Exception;
}
