package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.service.model.PathRewriteParser;
import com.ctrip.zeus.util.AssertUtils;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("locationConf")
public class LocationConf {
    @Resource
    ConfService confService;

    public String generate(Slb slb, VirtualServer vs, Group group, String upstreamName) throws Exception {
        StringBuilder b = new StringBuilder(1024);
        if (group.isVirtual()) {
            b.append("location ").append(getPath(slb, vs, group)).append(" {\n");
            if (group.getGroupVirtualServers().size() == 1) {
                addRedirectCommand(b, group);
            } else {
                throw new ValidationException("Virtual Group has Multiple Group VirtualServers Redirect");
            }
            b.append("}\n");
            return b.toString();
        }
        b.append("location ").append(getPath(slb, vs, group)).append(" {\n");

        Long slbId = slb.getId();
        Long vsId = vs.getId();
        Long groupId = group.getId();

        if (confService.getEnable("location.client.max.body.size", slbId, vsId, groupId, false)) {
            b.append("client_max_body_size ").append(confService.getStringValue("location.client.max.body.size", slbId, vsId, groupId, null)).append("m;\n");
        }

        b.append("proxy_request_buffering off;\n");
        b.append("proxy_next_upstream off;\n");

        b.append("proxy_set_header Host $host").append(";\n");
        b.append("proxy_set_header X-Real-IP $remote_addr;\n");

        addKeepAliveSettings(b,group.getId());
        addProxyReadTimeout(group.getId(),b);

        if (confService.getEnable("location.x-forwarded-for", slbId, vsId, groupId, true)){
            b.append("if ( $remote_addr ~* \"").append(confService.getStringValue("x-forwarded-for.white.list", slbId, vsId, groupId, "172\\..*|192\\.168.*|10\\..*")).append("\" ){\n")
                    .append("set $inWhite  \"true\";\n")
                    .append("}\n")
                    .append("rewrite_by_lua '\n")
                    .append("local headers = ngx.req.get_headers() ;\n")
                    .append("if ngx.var.inWhite ~= \"true\" or headers[\"X-Forwarded-For\"] == nil then\n")
                    .append("if (headers[\"True-Client-Ip\"] ~= nil) then\n")
                    .append("ngx.req.set_header(\"X-Forwarded-For\", headers[\"True-Client-IP\"])\n")
                    .append("else\n")
                    .append("ngx.req.set_header(\"X-Forwarded-For\", ngx.var.remote_addr )\n")
                    .append("end\n")
                    .append("end';\n");
        }else {
            b.append("proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n");
        }

        addErrorPageConfig(b,group.getId());
        b.append("set $upstream ").append(upstreamName).append(";\n");
        addBastionCommand(b,upstreamName);

        //rewrite should after set $upstream
        addRewriteCommand(b,slb,vs,group);
        if (group.getSsl()) {
            b.append("proxy_pass https://$upstream ;\n");
        }else {
            b.append("proxy_pass http://$upstream ;\n");
        }
        b.append("}").append("\n");
        return b.toString();
    }

    private void addKeepAliveSettings(StringBuilder b,Long groupId) throws Exception {
        if (confService.getEnable("location.upstream.keepAlive", null, null, groupId, false)) {
            b.append("proxy_set_header Connection \"\";\n");
        }
    }

    private void addProxyReadTimeout(Long groupId , StringBuilder sb) throws Exception {
        if (confService.getEnable("location.proxy.readTimeout", null, null, groupId, true)) {
            String readTimeout = confService.getStringValue("location.proxy.readTimeout", null, null, groupId, "60");
            sb.append("proxy_read_timeout ").append(readTimeout).append("s;\n");
        }
    }

    private static String getPath(Slb slb, VirtualServer vs, Group group) throws Exception{
        String res=null;
        for (GroupVirtualServer groupSlb : group.getGroupVirtualServers()) {
            if (slb.getId().equals(groupSlb.getVirtualServer().getSlbId()) && vs.getId().equals(groupSlb.getVirtualServer().getId())) {
                res= groupSlb.getPath();
            }
        }

        AssertUtils.assertNotNull(res, "Location path is null,Please check your configuration of SlbName:[" + slb.getName() + "] VirtualServer :[" + vs.getId() + "]");
        return res;
    }

    private static String getRewrite(Slb slb, VirtualServer vs, Group group) throws Exception{
        String res=null;
        for (GroupVirtualServer groupSlb : group.getGroupVirtualServers()) {
            if (slb.getId().equals(groupSlb.getVirtualServer().getSlbId()) && vs.getId().equals(groupSlb.getVirtualServer().getId())) {
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
            for (String tmp : rewriteList) {
                sb.append("rewrite ").append(tmp).append(" break;\n");
            }
        }
    }

    private static void addRedirectCommand(StringBuilder sb, Group group) throws Exception {
        if (sb != null) {
            String redirect = group.getGroupVirtualServers().get(0).getRedirect();
            if (redirect.isEmpty())
                return;
            List<String> rewriteList = PathRewriteParser.getValues(redirect);
            for (String tmp : rewriteList) {
                sb.append("rewrite ").append(tmp).append(" redirect;\n");
            }
        }
    }

    private void addBastionCommand(StringBuilder sb,String upstreamName) throws Exception {
        String whiteList = confService.getStringValue("bastion.white.list", null, null, null, "denyAll");
        sb.append("if ( $remote_addr ~* \"").append(whiteList).append("\")")
                .append("{\nset $upstream $cookie_bastion;\n}\n");
        sb.append("if ( $upstream = \"\")")
                .append("{\nset $upstream ").append(upstreamName).append(";\n}\n");
        sb.append("if ( $upstream != ").append(upstreamName).append(" ){\n")
                .append("add_header Bastion $cookie_bastion;\n}\n");
    }

    private void addErrorPageConfig(StringBuilder sb,Long groupId) throws Exception {
        if (confService.getEnable("location.errorPage", null, null, groupId, false)) {
            sb.append("proxy_intercept_errors on;\n");
        }
    }
}
