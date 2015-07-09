package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupSlb;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class LocationConf {
    private static DynamicStringProperty whiteList = DynamicPropertyFactory.getInstance().getStringProperty("bastion.white.list", null);
    private static DynamicStringProperty clientMaxSizeList = DynamicPropertyFactory.getInstance().getStringProperty("client.max.body.size.list", null);

    public static String generate(Slb slb, VirtualServer vs, Group group, String upstreamName)throws Exception {
        StringBuilder b = new StringBuilder(1024);

        b.append("location ").append(getPath(slb, vs, group)).append(" {\n");
        if (clientMaxSizeList !=null)
        {
            String []sizeList = clientMaxSizeList.get().split(";");
            String []groupSize = null;
            for (String tmp : sizeList)
            {
                groupSize = tmp.split("=");
                if (groupSize.length==2&&groupSize[0].equals(String.valueOf(group.getId())))
                {
                    b.append("client_max_body_size ").append(groupSize[1]).append("m;\n");
                }
            }
        }
        b.append("proxy_set_header Host $host").append(";\n");
        b.append("proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n");
        b.append("proxy_set_header X-Real-IP $remote_addr;\n");

        b.append("set $upstream ").append(upstreamName).append(";\n");
        addBastionCommand(b,upstreamName);
        //rewrite should after set $upstream
        addRewriteCommand(b,slb,vs,group);
        if (group.getSsl())
        {
            b.append("proxy_pass https://$upstream ;\n");
        }else {
            b.append("proxy_pass http://$upstream ;\n");
        }
        b.append("}").append("\n");

        return b.toString();
    }

    private static String getPath(Slb slb, VirtualServer vs, Group group) throws Exception{
        String res=null;
        for (GroupSlb groupSlb : group.getGroupSlbs()) {
            if (slb.getId().equals(groupSlb.getSlbId()) && vs.getId().equals(groupSlb.getVirtualServer().getId())) {
                res= groupSlb.getPath();
            }
        }

        AssertUtils.assertNotNull(res, "Location path is null,Please check your configuration of SlbName:[" + slb.getName() + "] VirtualServer :[" + vs.getId() + "]");
        return res;
    }

    private static String getRewrite(Slb slb, VirtualServer vs, Group group) throws Exception{
        String res=null;
        for (GroupSlb groupSlb : group.getGroupSlbs()) {
            if (slb.getId().equals(groupSlb.getSlbId()) && vs.getId().equals(groupSlb.getVirtualServer().getId())) {
                res= groupSlb.getRewrite();
            }
        }

        return res;
    }

    private static void addRewriteCommand(StringBuilder sb, Slb slb , VirtualServer vs , Group group) throws Exception {
        if (sb != null){
            String rewrite = getRewrite(slb,vs,group);
            if (rewrite==null || rewrite.isEmpty() || !rewrite.contains(" ")){
                return;
            }
            List<String> rewriteList = PathRewriteParser.getValues(rewrite);
            for (String tmp : rewriteList)
            {
                sb.append("rewrite ").append(tmp).append(" break;\n");
            }
//            String[] rewrites = rewrite.split(";");
//            for (int i = 0 ; i < rewrites.length ; i ++)
//            {
//                sb.append("rewrite ").append(rewrites[i]).append(" break;\n");
//            }
        }
    }
    private static void addBastionCommand(StringBuilder sb,String upstreamName){
        String wl = whiteList.get();
        if (null == wl || wl.isEmpty() || wl.trim().equals("") || wl.contains("\""))
        {
            wl="denyAll";
        }else if (wl.equals("allowAll")){
            wl="";
        }
        sb.append("if ( $remote_addr ~* \"").append(wl).append("\")")
                .append("{\nset $upstream $cookie_bastion;\n}\n");
        sb.append("if ( $upstream = \"\")")
                .append("{\nset $upstream ").append(upstreamName).append(";\n}\n");
        sb.append("if ( $upstream != ").append(upstreamName).append(" ){\n")
                .append("add_header Bastion $cookie_bastion;\n}\n");
    }
}
