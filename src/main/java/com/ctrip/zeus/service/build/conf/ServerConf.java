package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.service.build.ConfService;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.StringFormat;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
@Component("serverConf")
public class ServerConf {
    @Resource
    ConfService confService;
    @Resource
    UpstreamsConf upstreamsConf;
    @Resource
    LocationConf locationConf;

    public static final String SSL_PATH = "/data/nginx/ssl/";

    public String generate(Slb slb, VirtualServer vs, List<Group> groups) throws Exception {
        StringBuilder b = new StringBuilder(1024);

        AssertUtils.assertNotNull(vs.getPort(), "virtual server [" + vs.getId() + "] port is null! Please check the configuration!");
        try {
            Integer.parseInt(vs.getPort());
        } catch (Exception e) {
            throw new ValidationException("virtual server [" + vs.getId() + "] port is illegal!");
        }
        b.append("server {").append("\n");
        b.append("listen    ").append(vs.getPort()).append(";\n");
        b.append("server_name    ").append(getServerNames(vs)).append(";\n");
        b.append("ignore_invalid_headers off;\n");
        b.append("proxy_http_version 1.1;\n");
        addProxyBufferSize(b, vs.getId());

        if (vs.isSsl()) {
            b.append("ssl on;\n")
                    .append("ssl_certificate ").append(SSL_PATH).append(vs.getId()).append("/ssl.crt;\n")
                    .append("ssl_certificate_key ").append(SSL_PATH).append(vs.getId()).append("/ssl.key;\n");
        }
        addVirtualServerHealthCheck(b, vs);
        NginxConf.appendServerCommand(b);

        //add locations
        for (Group group : groups) {
            b.append(locationConf.generate(slb, vs, group, upstreamsConf.buildUpstreamName(vs, group)));
        }
        addErrorPage(b);
        b.append("}").append("\n");

        return StringFormat.format(b.toString());
    }

    private void addVirtualServerHealthCheck(StringBuilder b, VirtualServer vs) throws Exception {
        if (confService.getEnable("server.vs.health.check", null, vs.getId(), null, false)) {
            b.append("location ~* ^/do_not_delete/noc.gif$ {\n")
                    .append("add_header Accept-Ranges bytes;\n")
                    .append("content_by_lua '\n")
                    .append("local res = ngx.decode_base64(\"").append(confService.getStringValue("vs.health.check.gif.base64", null, vs.getId(), null, null)).append("\");\n")
                    .append("ngx.print(res);\n")
                    .append("return ngx.exit(200);\n';\n}\n");
        }
    }

    private void addProxyBufferSize(StringBuilder b, Long vsId) throws Exception {

        if (confService.getEnable("server.proxy.buffer.size", null, vsId, null, false)) {
            b.append("proxy_buffer_size ").append(confService.getStringValue("server.proxy.buffer.size", null, vsId, null, "8k")).append(";\n");
            b.append("proxy_buffers ").append(confService.getStringValue("server.proxy.buffers", null, vsId, null, "8 8k")).append(";\n");
            b.append("proxy_busy_buffers_size ").append(confService.getStringValue("server.proxy.busy.buffers.size", null, vsId, null, "8k")).append(";\n");
        }

    }

    private String getServerNames(VirtualServer vs) throws Exception {
        StringBuilder b = new StringBuilder(128);
        for (Domain domain : vs.getDomains()) {
            b.append(" ").append(domain.getName());
        }
        String res = b.toString();

        AssertUtils.assertNotEquals("", res.trim(), "virtual server [" + vs.getId() + "] domain is null or illegal!");
        return res;
    }

    private void addErrorPage(StringBuilder sb) throws Exception {
        if (!confService.getEnable("server.errorPage", null, null, null, false)) {
            return;
        }
        if (confService.getStringValue("server.errorPage.host.url", null, null, null, null) == null) {
            return;
        }

        for (int i = 400; i <= 425; i++) {
            writeErrorPage(sb, i);
        }
        for (int i = 500; i <= 510; i++) {
            writeErrorPage(sb, i);
        }
    }

    private void writeErrorPage(StringBuilder sb, int statusCode) throws Exception {
        if (confService.getEnable("errorPage.use.new", null, null, null, true)) {
            String path = "/" + statusCode + "page";
            sb.append("error_page ").append(statusCode).append(" ").append(path).append(";\n");
            sb.append("location = ").append(path).append(" {\n");
            sb.append("internal;\n");
            sb.append("proxy_set_header Accept ").append(confService.getStringValue("errorPage.accept", null, null, null, "text/html")).append(";\n");
            sb.append("rewrite_by_lua '\n");
            sb.append("local domain = \"domain=\"..ngx.var.host;\n");
            sb.append("local uri = \"&uri=\"..string.gsub(ngx.var.request_uri, \"?.*\", \"\");\n");
            sb.append("ngx.req.set_uri_args(domain..uri);';\n");
            sb.append("rewrite \"").append(path).append("\" \"").append("/errorpage/").append(statusCode).append("\" break;\n");
            sb.append("proxy_pass ").append(confService.getStringValue("errorPage.host.url", null, null, null, null)).append(";\n}\n");
        } else {
            DynamicStringProperty errorPageConfig = DynamicPropertyFactory.getInstance().getStringProperty("errorPage." + statusCode + ".url", null);
            if (null != errorPageConfig.get()) {
                String path = "/" + statusCode + "page";
                sb.append("error_page ").append(statusCode).append(" ").append(path).append(";\n");
                sb.append("location = ").append(path).append(" {\n");
                sb.append("internal;\n");
                sb.append("proxy_set_header Accept ").append(confService.getStringValue("errorPage.accept", null, null, null, "text/html")).append(";\n");
                sb.append("proxy_pass ").append(errorPageConfig.get()).append(";\n}\n");
            }
        }
    }
}
