package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.StringFormat;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class ServerConf {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConf.class);
    private static DynamicStringProperty allowSSL = DynamicPropertyFactory.getInstance().getStringProperty("virtual-server-id.ssl", "");
    public static final String SSL_PATH = "/data/nginx/ssl/";

    public static String generate(Slb slb, VirtualServer vs, List<Group> groups) throws Exception{
        StringBuilder b = new StringBuilder(1024);

        AssertUtils.assertNotNull(vs.getPort(), "virtual server [" + vs.getId() + "] port is null! Please check the configuration!");
        try{
            Integer.parseInt(vs.getPort());
        }catch (Exception e){
            throw new ValidationException("virtual server ["+vs.getId()+"] port is illegal!");
        }
        b.append("server {").append("\n");
        b.append("listen    ").append(vs.getPort()).append(";\n");
        b.append("server_name    ").append(getServerNames(vs)).append(";\n");
        b.append("ignore_invalid_headers off;\n");
        if (vs.getSsl())
        {
            String []sslList = allowSSL.get().split(";");
            for (String vsid : sslList)
            {
                if(String.valueOf(vs.getId()).equals(vsid.trim())){
                    b.append("ssl on;\n")
                     .append("ssl_certificate ").append(SSL_PATH).append(vs.getId()).append("/ssl.crt;\n")
                     .append("ssl_certificate_key ").append(SSL_PATH).append(vs.getId()).append("/ssl.key;\n");
                }
            }
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

        AssertUtils.assertNotEquals("", res.trim(), "virtual server [" + vs.getId() + "] domain is null or illegal!");
        return res;
    }
}
