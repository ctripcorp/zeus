package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.StringFormat;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class ServerConf {

    public static String generate(Slb slb, VirtualServer vs, List<App> apps) throws Exception{
        StringBuilder b = new StringBuilder(1024);

        AssertUtils.isNull(vs.getPort(),"virtual server ["+vs.getName()+"] port is null! Please check the configuration!");
        try{
            Integer.parseInt(vs.getPort());
        }catch (Exception e){
            throw new ValidationException("virtual server ["+vs.getName()+"] port is illegal!");
        }


        b.append("server {").append("\n");
        b.append("listen    ").append(vs.getPort()).append(";\n");
        b.append("server_name    ").append(getServerNames(vs)).append(";\n");

        NginxConf.appendServerCommand(b);
        //add locations
        for (App app : apps) {
            b.append(LocationConf.generate(slb, vs, app, UpstreamsConf.buildUpstreamName(slb, vs, app)));
        }

        b.append("}").append("\n");

        return StringFormat.format(b.toString());
    }

    private static String getServerNames(VirtualServer vs) throws Exception{
        StringBuilder b = new StringBuilder(128);
        for (Domain domain : vs.getDomains()) {
            b.append(" ").append(domain.getName());
        }
        String res =  b.toString();

        AssertUtils.arrertNotEquels("",res.trim().isEmpty(),"virtual server ["+vs.getName()+"] domain is null or illegal!");
        return res;
    }
}
