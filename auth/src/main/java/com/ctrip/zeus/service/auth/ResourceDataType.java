package com.ctrip.zeus.service.auth;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by fanqq on 2016/8/1.
 */
public enum ResourceDataType {
    Group,
    Policy,
    Dr,
    Rule,
    Flow,
    Slb,
    Vs,
    Ip,
    Clean,
    Lock,
    Conf,
    SyncError,
    Auth,
    Cert,
    Config,
    User;
    private static Set<String> names = new HashSet<>();
    static {
        for (ResourceDataType r : values()){
            names.add(r.getType());
        }
    }

    public String getType(){
        return name();
    }

    ResourceDataType getByName(String name){
        return valueOf(name);
    }

    static public boolean contain(String name){
        return names.contains(name);
    }

    static public String getNames(){
        return names.toString();
    }
}
