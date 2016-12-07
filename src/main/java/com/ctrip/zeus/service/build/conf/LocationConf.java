package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.build.ConfigHandler;
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
    ConfigHandler configHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationConf.class);

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
            .append("end'").toString();
    private final String newSetHeaderLuaScripts = new StringBuilder(500)
            .append("'\n")
            .append("  local headers = ngx.req.get_headers();\n")
            .append("  if ngx.var.inWhite ~= \"true\" or headers[\"X-Forwarded-For\"] == nil then\n")
            .append("    if (headers[\"True-Client-Ip\"] ~= nil) then\n")
            .append("      ngx.req.clear_header(\"X-Forwarded-For\")\n")
            .append("      ngx.req.set_header(\"X-Forwarded-For\", headers[\"True-Client-IP\"])\n")
            .append("    else\n")
            .append("      ngx.req.clear_header(\"X-Forwarded-For\")\n")
            .append("      ngx.req.set_header(\"X-Forwarded-For\", ngx.var.remote_addr )\n")
            .append("  end\n")
            .append("end'").toString();

    public void write(ConfWriter confWriter, Slb slb, VirtualServer vs, Group group) throws Exception {
        Long slbId = slb.getId();
        Long vsId = vs.getId();
        Long groupId = group.getId();

        for (GroupVirtualServer e : group.getGroupVirtualServers()) {
            if (e.getVirtualServer().getId().longValue() == vs.getId().longValue()) {
                String upstreamName = "backend_" + group.getId();

                // TODO confirm path is not empty

                // if group is virtual
                if (group.isVirtual()) {
                    writeVirtualLocation(confWriter, e.getPath(), group);
                    continue;
                }
                if (configHandler.getEnable("socket.io.group", null, null, groupId, false)) {
                    writeSocketIOGroup(confWriter, e.getPath(), group, upstreamName);
                    continue;
                }

                // if group is not virtual
                confWriter.writeLocationStart(e.getPath());
                if (configHandler.getEnable("location.client.max.body.size", slbId, vsId, groupId, false)) {
                    confWriter.writeCommand("client_max_body_size", configHandler.getStringValue("location.client.max.body.size", slbId, vsId, groupId, "2") + "m");
                }

                confWriter.writeCommand("client_body_buffer_size", configHandler.getStringValue("location.client.body.buffer.size", slbId, vsId, groupId, "16k"));

                if (configHandler.getEnable("location.gzip", slbId, vsId, groupId, false)) {
                    confWriter.writeCommand("gzip", "on");
                    confWriter.writeCommand("gzip_types", configHandler.getStringValue("location.gzip.types", slbId, vsId, groupId, "text/html"));
                    confWriter.writeCommand("gzip_min_length", configHandler.getStringValue("location.gzip.min.length", slbId, vsId, groupId, "100"));
                    confWriter.writeCommand("gzip_comp_level", configHandler.getStringValue("location.gzip.comp.level", slbId, vsId, groupId, "1"));
                    confWriter.writeCommand("gzip_buffers", configHandler.getStringValue("location.gzip.buffers", slbId, vsId, groupId, "16 8k"));
                }

                // write proxy configuration
                if (vs.isSsl() && configHandler.getEnable("http.version.2", null, null, null, false)) {
                    confWriter.writeCommand("proxy_request_buffering", "on");
                } else {
                    confWriter.writeCommand("proxy_request_buffering", "off");
                }

                confWriter.writeCommand("proxy_next_upstream", "off");

                confWriter.writeCommand("proxy_set_header", "Host $host");
                confWriter.writeCommand("proxy_set_header", "X-Real-IP $remote_addr");

                if (configHandler.getEnable("location.upstream.keepAlive", slbId, vsId, groupId, false)) {
                    confWriter.writeCommand("proxy_set_header", "Connection \"\"");
                }
                String proxyReadTimeout = "location.proxy.readTimeout";
                if (configHandler.getEnable(proxyReadTimeout, slbId, vsId, groupId, true)) {
                    String readTimeout = configHandler.getStringValue(proxyReadTimeout, slbId, vsId, groupId, "60");
                    confWriter.writeCommand("proxy_read_timeout", readTimeout + "s");
                }

                // write x-forward-for configuration
                if (configHandler.getEnable("location.x-forwarded-for", slbId, vsId, groupId, true)) {
                    confWriter.writeIfStart("$remote_addr ~* \"" +
                            configHandler.getStringValue("location.x-forwarded-for.white.list", slbId, vsId, groupId, "172\\..*|192\\.168.*|10\\..*") + "\"")
                            .writeCommand("set", "$inWhite \"true\"")
                            .writeIfEnd();
                    if (configHandler.getEnable("new_xff_header", slbId, vsId, groupId, false)) {
                        confWriter.writeCommand("rewrite_by_lua", newSetHeaderLuaScripts);
                    } else {
                        confWriter.writeCommand("rewrite_by_lua", setHeaderLuaScripts);
                    }
                } else {
                    confWriter.writeCommand("proxy_set_header", "X-Forwarded-For $proxy_add_x_forwarded_for");
                }

                // write error page configuration if defined
                if (configHandler.getEnable("location.errorPage", slbId, vsId, groupId, false)) {
                    confWriter.writeCommand("proxy_intercept_errors", "on");
                }

                // set upstream value
                confWriter.writeCommand("set", "$upstream " + upstreamName);
                confWriter.writeCommand("set", LogFormat.VAR_UPSTREAM_NAME + " " + upstreamName);

                addBastionCommand(confWriter, upstreamName, slbId, vsId, groupId);

                //rewrite should be after $upstream
                addRewriteCommand(confWriter, vs, group);
                if (group.isSsl()) {
                    confWriter.writeCommand("proxy_pass", "https://$upstream");
                } else {
                    confWriter.writeCommand("proxy_pass", "http://$upstream");
                }
                confWriter.writeLocationEnd();
            }
        }
    }

    private void writeSocketIOGroup(ConfWriter confWriter, String path, Group group, String upstreamName) {
        confWriter.writeLocationStart(path);
        confWriter.writeCommand("proxy_set_header", "Upgrade $http_upgrade");
        confWriter.writeCommand("proxy_set_header", "Connection \"upgrade\"");
        confWriter.writeCommand("proxy_set_header", "X-Forwarded-For $proxy_add_x_forwarded_for");
        confWriter.writeCommand("proxy_set_header", "Host $host");
        confWriter.writeCommand("proxy_http_version", "1.1");
        confWriter.writeCommand("proxy_set_header", "X-Real-IP $remote_addr");
        // set upstream value
        confWriter.writeCommand("set", "$upstream " + upstreamName);
        confWriter.writeCommand("set", LogFormat.VAR_UPSTREAM_NAME + " " + upstreamName);
        if (group.isSsl()) {
            confWriter.writeCommand("proxy_pass", "https://$upstream");
        } else {
            confWriter.writeCommand("proxy_pass", "http://$upstream");
        }

        confWriter.writeLocationEnd();
    }

    private static void writeVirtualLocation(ConfWriter confWriter, String path, Group group) throws Exception {
        confWriter.writeLocationStart(path);
        //TODO virtual group cannot have multi group-vs redirect
        if (group.getGroupVirtualServers().size() == 1) {
            addRedirectCommand(confWriter, group);
        }
        confWriter.writeLocationEnd();
    }

    private static String getRewrite(VirtualServer vs, Group group) throws Exception {
        String res = null;
        for (GroupVirtualServer groupSlb : group.getGroupVirtualServers()) {
            if (groupSlb.getVirtualServer().getId().longValue() == vs.getId().longValue()) {
                res = groupSlb.getRewrite();
            }
        }
        return res;
    }

    private static void addRewriteCommand(ConfWriter confWriter, VirtualServer vs, Group group) throws Exception {
        if (confWriter != null) {
            String rewrite = getRewrite(vs, group);
            if (rewrite == null || rewrite.isEmpty()) {
                return;
            }
            List<String> rewriteList = PathRewriteParser.getValues(rewrite);
            for (String tmp : rewriteList) {
                confWriter.writeCommand("rewrite", tmp + " break");
            }
        }
    }

    private static void addRedirectCommand(ConfWriter confWriter, Group group) throws Exception {
        if (confWriter != null) {
            String redirect = group.getGroupVirtualServers().get(0).getRedirect();
            if (redirect.isEmpty())
                return;
            List<String> redirectList = PathRewriteParser.getValues(redirect);
            for (String tmp : redirectList) {
                confWriter.writeCommand("rewrite", tmp + " redirect");
            }
        }
    }

    private void addBastionCommand(ConfWriter confWriter, String upstreamName, Long slbId, Long vsId, Long groupId) throws Exception {
        String whiteList = configHandler.getStringValue("location.bastion.white.list", slbId, vsId, groupId, "denyAll");

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

    public void writeHealthCheckLocation(ConfWriter confWriter, Long slbId, Long vsId) throws Exception {
        confWriter.writeLocationStart("~* ^/do_not_delete/noc.gif$");
        confWriter.writeCommand("add_header", "Accept-Ranges bytes");
        confWriter.writeCommand("content_by_lua", getHcLuaScripts(slbId, vsId));
        confWriter.writeLocationEnd();
    }

    private String getHcLuaScripts(Long slbId, Long vsId) throws Exception {
        return new StringBuilder(512).append("'\n")
                //TODO hardcode health check gif
                .append("local res = ngx.decode_base64(\"").append(configHandler.getStringValue("location.vs.health.check.gif.base64", slbId, vsId, null, "")).append("\");\n")
                .append("ngx.print(res);\n")
                .append("return ngx.exit(200);'").toString();
    }

    public void writeErrorPageLocation(ConfWriter confWriter, boolean errorPageUseNew,
                                       int statusCode, Long slbId, Long vsId) throws Exception {
        String errorPageAccept = configHandler.getStringValue("location.errorPage.accept", slbId, vsId, null, "text/html");
        if (errorPageUseNew) {
            String url = configHandler.getStringValue("location.errorPage.host.url", slbId, vsId, null, null);
            if (url != null && !url.isEmpty()) {
                String path = "/" + statusCode + "page";
                confWriter.writeCommand("error_page", statusCode + " " + path);
                confWriter.writeLocationStart("= " + path);
                confWriter.writeLine("internal;");
                confWriter.writeCommand("proxy_set_header Accept", errorPageAccept);
                confWriter.writeCommand("rewrite_by_lua", errLuaScripts);
                confWriter.writeCommand("rewrite", "\"" + path + "\" \"/errorpage/" + statusCode + "\" break");
                confWriter.writeCommand("proxy_pass", url);
                confWriter.writeLocationEnd();
                return;
            } else {
                LOGGER.info("Error page url is not configured. key:location.errorPage.host.url; vsId:" + vsId);
            }
        } else {
            String errorPageConfig = configHandler.getStringValue("location.errorPage." + statusCode + ".url", slbId, vsId, null, null);
            if (errorPageConfig != null && !errorPageConfig.isEmpty()) {
                String path = "/" + statusCode + "page";
                confWriter.writeCommand("error_page", statusCode + " " + path);
                confWriter.writeLocationStart("= " + path);
                confWriter.writeLine("internal;");
                confWriter.writeCommand("proxy_set_header Accept", errorPageAccept);
                confWriter.writeCommand("proxy_pass", errorPageConfig);
                confWriter.writeLocationEnd();
                return;
            } else {
                LOGGER.info("Error page url is not configured. key:location.errorPage." + statusCode + ".url; vsId:" + vsId);
            }
        }
        // Use Default error page.
        if (configHandler.getEnable("nginx.default.error.page", slbId, vsId, null, false)) {
            String path = "/" + statusCode + "page";
            confWriter.writeCommand("error_page", statusCode + " " + path);
            confWriter.writeLocationStart("= " + path);
            confWriter.writeLine("internal;");
            confWriter.writeCommand("rewrite", "\"/(.*)\" \"/$1.html\" break");
            confWriter.writeCommand("root", configHandler.getStringValue("error.page.root.path", "/opt/app/nginx/conf/errorpage"));
            confWriter.writeLocationEnd();
        }
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
        confWriter.writeCommand("set", LogFormat.VAR_UPSTREAM_NAME + " 127.0.0.1");
        confWriter.writeLine("return 404 \"Not Found!\";");
        confWriter.writeLocationEnd();
    }
}
