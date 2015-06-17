package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Group;
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

    public static String generate(Slb slb, VirtualServer vs, List<Group> groups) throws Exception{
        StringBuilder b = new StringBuilder(1024);

        AssertUtils.isNull(vs.getPort(),"virtual server ["+vs.getId()+"] port is null! Please check the configuration!");
        try{
            Integer.parseInt(vs.getPort());
        }catch (Exception e){
            throw new ValidationException("virtual server ["+vs.getId()+"] port is illegal!");
        }


        b.append("server {").append("\n");
        b.append("listen    ").append(vs.getPort()).append(";\n");
        b.append("server_name    ").append(getServerNames(vs)).append(";\n");
        if (vs.getSsl())
        {
            b.append("ssl on;\n")
             .append("ssl_certificate /data/nginx/").append(vs.getId()).append("/ssl.crt;\n")
             .append("ssl_certificate_key /data/nginx/").append(vs.getId()).append("/ssl.key;\n");
        }
        NginxConf.appendServerCommand(b);
        //add locations
        for (Group group : groups) {
            b.append(LocationConf.generate(slb, vs, group, UpstreamsConf.buildUpstreamName(slb, vs, group)));
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

        AssertUtils.assertNotEquels("",res.trim(),"virtual server ["+vs.getId()+"] domain is null or illegal!");
        return res;
    }
}
