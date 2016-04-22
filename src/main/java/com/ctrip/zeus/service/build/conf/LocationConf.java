package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.service.model.PathRewriteParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Long slbId = null;
    private Long vsId = null;
    private Long groupId = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationConf.class);

    private final String hcLuaScripts = new StringBuilder(512).append("'\n")
            //TODO hardcode health check gif
            .append("local res = ngx.decode_base64(\"").append("").append("\");\n")
            .append("ngx.print(res);\n")
            .append("return ngx.exit(200);'").toString();
    private final String errLuaScripts = new StringBuilder(512).append("'\n")
            .append("local domain = \"domain=\"..ngx.var.host;\n")
            .append("local uri = \"&uri=\"..string.gsub(ngx.var.request_uri, \"?.*\", \"\");\n")
            .append("ngx.req.set_uri_args(domain..uri);'").toString();
    private final String setHeaderLuaScripts = new StringBuilder(500)
            .append("'\n")
            .append("  local headers = ngx.req.get_headers();\n")
            .append("  if ngx.var.inWhite ~= \"true\" or headers[\"X-Forwarded-For\"] == nil then\n")
            .append("    if (headers[\"True-Client-Ip\"] ~= nil) then\n")
            .append("      ngx.req.set_header(\"X-Forwarded-For\", headers[\"True-Client-IP\"])\n")
            .append("    else\n")
            .append("      ngx.req.set_header(\"X-Forwarded-For\", ngx.var.remote_addr )\n")
            .append("  end\n")
            .append("end';\n").toString();

    public void write(ConfWriter confWriter, Slb slb, VirtualServer vs, Group group) throws Exception {
        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
            if (e.getVirtualServer().getSlbId().longValue() == slb.getId()) {
                String upstreamName = "backend_" + group.getId();
                // TODO confirm path is not empty
                if (group.isVirtual()) {
                    //TODO virtual group cannot have multi group-vs redirect
                    confWriter.writeLocationStart(e.getPath());
                    if (group.getGroupVirtualServers().size() == 1) {
                        addRedirectCommand(confWriter.getStringBuilder(), group);
                    }
                } else {
                    confWriter.writeLocationStart(e.getPath());

                    if (confService.getEnable("location.client.max.body.size", slbId, vsId, groupId, false)) {
                        confWriter.writeCommand("client_max_body_size", confService.getStringValue("location.client.max.body.size", slbId, vsId, groupId, null) + "m");
                    }

                    confWriter.writeCommand("proxy_request_buffering", "off");
                    confWriter.writeCommand("proxy_next_upstream", "off");

                    confWriter.writeCommand("proxy_set_header", "Host $host");
                    confWriter.writeCommand("proxy_set_header", "X-Real-IP $remote_addr");

                    if (confService.getEnable("location.upstream.keepAlive", slbId, vsId, groupId, false)) {
                        confWriter.writeCommand("proxy_set_header", "Connection \"\"");
                    }
                    String proxyReadTimeout = "location.proxy.readTimeout";
                    if (confService.getEnable(proxyReadTimeout, slbId, vsId, groupId, true)) {
                        String readTimeout = confService.getStringValue(proxyReadTimeout, slbId, vsId, groupId, "60");
                        confWriter.writeCommand("proxy_read_timeout", readTimeout + "s");
                    }

                    if (confService.getEnable("location.x-forwarded-for", slbId, vsId, groupId, true)){
                        confWriter.writeIfStart("$remote_addr ~* \"" +
                                confService.getStringValue("location.x-forwarded-for.white.list", slbId, vsId, groupId, "172\\..*|192\\.168.*|10\\..*") + "\"")
                                .writeCommand("set", "$inWhite \"true\"")
                                .writeIfEnd()
                                .write("rewrite_by_lua ").write(setHeaderLuaScripts);
                    }else {
                        confWriter.writeCommand("proxy_set_header", "X-Forwarded-For $proxy_add_x_forwarded_for");
                    }

                    if (confService.getEnable("location.errorPage", slbId, vsId, groupId, false)) {
                        confWriter.writeCommand("proxy_intercept_errors", "on");
                    }

                    confWriter.writeCommand("set", "$upstream " + upstreamName);
                    addBastionCommand(confWriter,upstreamName);

                    //rewrite should after set $upstream
                    addRewriteCommand(confWriter, slb, vs, group);
                    if (group.getSsl()) {
                        confWriter.writeCommand("proxy_pass", "https://$upstream");
                    }else {
                        confWriter.writeCommand("proxy_pass", "http://$upstream");
                    }
                    confWriter.writeLocationEnd();
                }
            }
        }
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

    private static void addRewriteCommand(ConfWriter confWriter, Slb slb , VirtualServer vs , Group group) throws Exception {
        if (confWriter != null){
            String rewrite = getRewrite(slb, vs, group);
            if (rewrite==null || rewrite.isEmpty() || !rewrite.contains(" ")){
                return;
            }
            List<String> rewriteList = PathRewriteParser.getValues(rewrite);
            for (String tmp : rewriteList) {
                confWriter.writeCommand("rewrite", tmp + " break");
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

    private void addBastionCommand(ConfWriter confWriter,String upstreamName) throws Exception {
        String whiteList = confService.getStringValue("location.bastion.white.list", slbId, vsId, groupId, "denyAll");

        confWriter.writeIfStart("$remote_addr ~* \"" + whiteList + "\"")
                .writeCommand("set", "$upstream $cookie_bastion")
                .writeIfEnd();
        confWriter.writeIfStart("$upstream = \"\"")
                .writeCommand("set", "$upstream " + upstreamName)
                .writeIfEnd();
        confWriter.writeIfStart("$upstream != " + upstreamName)
                .writeCommand("add_header", "Bastion $cookie_bastion")
                .writeIfEnd();
    }

    public void writeHealthCheckLocation(ConfWriter confWriter) {
        confWriter.writeLocationStart("~* ^/do_not_delete/noc.gif$");
        confWriter.writeCommand("add_header", "Accept-Ranges bytes");
        confWriter.writeCommand("content_by_lua", hcLuaScripts);
        confWriter.writeLocationEnd();
    }

    public void writeErrorPageLocation(ConfWriter confWriter, int statusCode) throws Exception {
        String url = confService.getStringValue("location.errorPage.host.url", slbId, vsId, null, null);
        if (url == null || url.isEmpty()) {
            LOGGER.error("Error page url is not configured. Skip writing error page locations.");
            return;
        }

        String path = "/" + statusCode + "page";
        confWriter.writeCommand("error_page", statusCode + " " + path);
        confWriter.writeLocationStart("= " + path);
        confWriter.writeLine("internal;");
        confWriter.writeCommand("proxy_set_header", "Accept text/html");
        confWriter.writeCommand("rewrite_by_lua", errLuaScripts);
        confWriter.writeCommand("rewrite", "\"" + path + "\" \"/errorpage/" + statusCode + "\" break");
        confWriter.writeCommand("proxy_pass", url);
        confWriter.writeLocationEnd();
    }

    public void writeDyupsLocation(ConfWriter confWriter) {
        confWriter.writeLocationStart("/");
        confWriter.writeLine("dyups_interface;");
        confWriter.writeLocationEnd();
    }

    public void writeCheckStatusLocations(ConfWriter confWriter) {
        confWriter.writeLocationStart("=/status.json");
        confWriter.writeCommand("add_header", "Access-Control-Allow-Origin *");
        confWriter.writeCommand("check_status", "json");
        confWriter.writeLocationEnd();

        confWriter.writeLocationStart("/");
        confWriter.writeCommand("add_header", "Access-Control-Allow-Origin *");
        confWriter.writeLine("check_status;");
        confWriter.writeLocationEnd();

        confWriter.writeLocationStart("/req_status");
        confWriter.writeLine("req_status_show;");
        confWriter.writeLocationEnd();

        confWriter.writeLocationStart("/stub_status");
        confWriter.writeCommand("stub_status", "on");
        confWriter.writeLocationEnd();
    }

    public void writeDefaultLocations(ConfWriter confWriter) {
        confWriter.writeLocationStart("= /domaininfo/OnService.html");
        confWriter.writeCommand("add_header", "Content-Type text/html");
        confWriter.writeLine("return 200 \"4008206666\";");
        confWriter.writeLocationEnd();

        confWriter.writeLocationStart("/");
        confWriter.writeLine("return 404 \"Not Found!\";");
        confWriter.writeLocationEnd();
    }
}
