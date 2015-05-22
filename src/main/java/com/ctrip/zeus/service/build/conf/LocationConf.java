package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.App;
import com.ctrip.zeus.model.entity.AppSlb;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class LocationConf {
    public static String generate(Slb slb, VirtualServer vs, App app, String upstreamName)throws Exception {
        StringBuilder b = new StringBuilder(1024);

        b.append("location ").append(getPath(slb, vs, app)).append("{\n");
        b.append("proxy_set_header Host $host").append(";\n");
        addRewriteCommand(b,slb,vs,app);
        b.append("set $upstream ").append(upstreamName).append(";\n");
        if (app.getSsl())
        {
            b.append("proxy_pass https://$upstream ;\n");
        }else {
            b.append("proxy_pass http://$upstream ;\n");
        }
        b.append("proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n");
        b.append("proxy_set_header X-Real-IP $remote_addr;");

        b.append("}").append("\n");

        return b.toString();
    }

    private static String getPath(Slb slb, VirtualServer vs, App app) throws Exception{
        String res=null;
        for (AppSlb appSlb : app.getAppSlbs()) {
            if (slb.getName().equals(appSlb.getSlbName()) && vs.getName().equals(appSlb.getVirtualServer().getName())) {
                res= appSlb.getPath();
            }
        }

        AssertUtils.isNull(res,"Location path is null,Please check your configuration of SlbName:["+slb.getName()+"] VirtualServer :["+vs.getName()+"]");
        return res;
    }

    private static String getRewrite(Slb slb, VirtualServer vs, App app) throws Exception{
        String res=null;
        for (AppSlb appSlb : app.getAppSlbs()) {
            if (slb.getName().equals(appSlb.getSlbName()) && vs.getName().equals(appSlb.getVirtualServer().getName())) {
                res= appSlb.getRewrite();
            }
        }

        return res;
    }

    private static void addRewriteCommand(StringBuilder sb, Slb slb , VirtualServer vs , App app) throws Exception {
        if (sb != null){
            String rewrite = getRewrite(slb,vs,app);
            if (rewrite==null){
                return;
            }
            String[] rewrites = rewrite.split(";");
            for (int i = 0 ; i < rewrites.length ; i ++)
            {
                sb.append("rewrite ").append(rewrites[i]).append(" break;\n");
            }
        }
    }
}
