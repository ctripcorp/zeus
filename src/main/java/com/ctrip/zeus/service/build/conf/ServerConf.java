package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.Domain;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.StringFormat;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
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
    private static DynamicStringProperty errorPage_404 = DynamicPropertyFactory.getInstance().getStringProperty("errorPage.404.url", null);//"http://slberrorpages.ctripcorp.com/slberrorpages/404.htm");
    private static DynamicStringProperty errorPage_500 = DynamicPropertyFactory.getInstance().getStringProperty("errorPage.500.url", null);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicBooleanProperty errorPageEnable = DynamicPropertyFactory.getInstance().getBooleanProperty("errorPage.enable", false);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicBooleanProperty errorPageUseNew = DynamicPropertyFactory.getInstance().getBooleanProperty("errorPage.use.new", true);
    private static DynamicStringProperty proxyBufferSizeWhiteList = DynamicPropertyFactory.getInstance().getStringProperty("proxy.buffer.size.white-list", null);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");
    private static DynamicBooleanProperty proxyBufferSizeEnableAll = DynamicPropertyFactory.getInstance().getBooleanProperty("proxy.buffer.size.enableAll", false);
    private static DynamicStringProperty proxyBufferSize = DynamicPropertyFactory.getInstance().getStringProperty("proxy.buffer.size", "8k");
    private static DynamicStringProperty proxyBuffers = DynamicPropertyFactory.getInstance().getStringProperty("proxy.buffers", "8 8k");
    private static DynamicStringProperty busyProxyBuffer = DynamicPropertyFactory.getInstance().getStringProperty("proxy.busy.buffers.size", "8k");
    private static DynamicStringProperty errorPageAccept = DynamicPropertyFactory.getInstance().getStringProperty("errorPage.accept", "text/html");
    private static DynamicBooleanProperty vsHealthCheckEnalbe = DynamicPropertyFactory.getInstance().getBooleanProperty("vs.health.check.enable", false);
    private static DynamicBooleanProperty vsHealthCheckEnalbeAll = DynamicPropertyFactory.getInstance().getBooleanProperty("vs.health.check.enable.all", false);
    private static DynamicStringProperty vsHealthCheckGif = DynamicPropertyFactory.getInstance().getStringProperty("vs.health.check.gif.base64", null);
    private static DynamicStringProperty errorPageHost = DynamicPropertyFactory.getInstance().getStringProperty("errorPage.host.url", null);//"http://slberrorpages.ctripcorp.com/slberrorpages/500.htm");


    public static final String SSL_PATH = "/data/nginx/ssl/";

    public static String generate(Slb slb, VirtualServer vs, List<Group> groups) throws Exception {
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

        if (vs.getSsl()) {
            b.append("ssl on;\n")
                    .append("ssl_certificate ").append(SSL_PATH).append(vs.getId()).append("/ssl.crt;\n")
                    .append("ssl_certificate_key ").append(SSL_PATH).append(vs.getId()).append("/ssl.key;\n");
        }
        addVirtualServerHealthCheck(b, vs);
        NginxConf.appendServerCommand(b);
        //add locations
        for (Group group : groups) {
            b.append(LocationConf.generate(slb, vs, group, UpstreamsConf.buildUpstreamName(slb, vs, group)));
        }
        addErrorPage(b);
        b.append("}").append("\n");

        return StringFormat.format(b.toString());
    }

    private static void addVirtualServerHealthCheck(StringBuilder b, VirtualServer vs) {
        if (vsHealthCheckEnalbe.get()) {
            if (vsHealthCheckEnalbeAll.get()) {
                b.append("location ~* ^/do_not_delete/noc.gif$ {\n")
                        .append("add_header Accept-Ranges bytes;\n")
                        .append("content_by_lua '\n")
                        .append("local res = ngx.decode_base64(\"").append(vsHealthCheckGif.get()).append("\");\n")
                        .append("ngx.print(res);\n")
                        .append("return ngx.exit(200);\n';\n}\n");
            } else {
                String key = "vs.health.check.enable." + vs.getId();
                DynamicBooleanProperty enable = DynamicPropertyFactory.getInstance().getBooleanProperty(key, false);
                if (enable.get()) {
                    b.append("location ~* ^/do_not_delete/noc.gif$ {\n")
                            .append("add_header Accept-Ranges bytes;\n")
                            .append("content_by_lua '\n")
                            .append("local res = ngx.decode_base64(\"").append(vsHealthCheckGif.get()).append("\");\n")
                            .append("ngx.print(res);\n")
                            .append("return ngx.exit(200);\n';\n}\n");
                }
            }
        }
    }

    private static void addProxyBufferSize(StringBuilder b, Long id) {
        boolean needAdd = false;
        if (proxyBufferSizeEnableAll.get()) {
            needAdd = true;
        } else if (proxyBufferSizeWhiteList.get() != null) {
            String[] vsids = proxyBufferSizeWhiteList.get().split(";");
            for (String vsId : vsids) {
                if (String.valueOf(id).equals(vsId.trim())) {
                    needAdd = true;
                }
            }
        }
        if (needAdd) {
            b.append("proxy_buffer_size ").append(proxyBufferSize.get()).append(";\n");
            b.append("proxy_buffers ").append(proxyBuffers.get()).append(";\n");
            b.append("proxy_busy_buffers_size ").append(busyProxyBuffer.get()).append(";\n");
        }
    }

    private static String getServerNames(VirtualServer vs) throws Exception {
        StringBuilder b = new StringBuilder(128);
        for (Domain domain : vs.getDomains()) {
            b.append(" ").append(domain.getName());
        }
        String res = b.toString();

        AssertUtils.assertNotEquals("", res.trim(), "virtual server [" + vs.getId() + "] domain is null or illegal!");
        return res;
    }

    private static void addErrorPage(StringBuilder sb) {
        if (!errorPageEnable.get()) {
            return;
        }
        if (errorPageHost.get() == null) {
            return;
        }
        for (int i = 400; i <= 425; i++) {
            writeErrorPage(sb, i);
        }
        for (int i = 500; i <= 510; i++) {
            writeErrorPage(sb, i);
        }
    }

    private static void writeErrorPage(StringBuilder sb, int statusCode) {
        if (errorPageUseNew.get()) {
            String path = "/" + statusCode + "page";
            sb.append("error_page ").append(statusCode).append(" ").append(path).append(";\n");
            sb.append("location = ").append(path).append(" {\n");
            sb.append("internal;\n");
            sb.append("proxy_set_header Accept ").append(errorPageAccept.get()).append(";\n");
            sb.append("rewrite_by_lua '\n");
            sb.append("local domain = \"domain=\"..ngx.var.host;\n");
            sb.append("local uri = \"&uri=\"..string.gsub(ngx.var.request_uri, \"?.*\", \"\");\n");
            sb.append("ngx.req.set_uri_args(domain..uri);';\n");
            sb.append("rewrite \"").append(path).append("\" \"").append("/errorpage/").append(statusCode).append("\" break;\n");
            sb.append("proxy_pass ").append(errorPageHost.get()).append(";\n}\n");
        } else {
            DynamicStringProperty errorPageConfig = DynamicPropertyFactory.getInstance().getStringProperty("errorPage." + statusCode + ".url", null);
            if (null != errorPageConfig.get()) {
                String path = "/" + statusCode + "page";
                sb.append("error_page ").append(statusCode).append(" ").append(path).append(";\n");
                sb.append("location = ").append(path).append(" {\n");
                sb.append("internal;\n");
                sb.append("proxy_set_header Accept ").append(errorPageAccept.get()).append(";\n");
                sb.append("proxy_pass ").append(errorPageConfig.get()).append(";\n}\n");
            }
        }
    }
}
