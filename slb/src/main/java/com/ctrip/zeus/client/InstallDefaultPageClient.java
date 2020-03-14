package com.ctrip.zeus.client;

import com.ctrip.zeus.dao.entity.FileData;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.page.DefaultFile;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ctrip.zeus.auth.util.AuthTokenUtil.getDefaultHeaders;

/**
 * Created by fanqq on 2016/8/22.
 */
public class InstallDefaultPageClient extends AbstractRestClient {
    private static DynamicStringProperty agentUrl = DynamicPropertyFactory.getInstance().getStringProperty("agent.api.host", "http://localhost:8099");
    private static LoadingCache<String, InstallDefaultPageClient> cache = CacheBuilder.newBuilder().maximumSize(10)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build(new CacheLoader<String, InstallDefaultPageClient>() {
                       @Override
                       public InstallDefaultPageClient load(String url) throws Exception {
                           return new InstallDefaultPageClient(url);
                       }
                   }
            );

    private static DynamicIntProperty adminServerPort = DynamicPropertyFactory.getInstance().getIntProperty("server.port", 8099);

    protected InstallDefaultPageClient(String url) {
        super(url);
    }

    protected InstallDefaultPageClient(String url, int readTimeout) {
        super(url, readTimeout);
    }

    public static InstallDefaultPageClient getClient(String url) throws Exception {
        return cache.get(url);
    }

    public static InstallDefaultPageClient getClientByServerIp(String serverIp) throws Exception {
        return getClient("http://" + serverIp + ":" + adminServerPort.get());
    }

    public static InstallDefaultPageClient getClientByHost(String host) throws Exception {
        return getClient(host);
    }

    /**
     * File install
     */

    // Install File
    public boolean installFile(String fileName, byte[] data) throws Exception {
        if (data == null || data.length == 0) throw new ValidationException("FIle data shall not be empty");
        // todo. new/ reason: for compatible old version
        WebTarget target = getTarget().path("/api/file/new/install/local");
        target = target.queryParam("fileName", fileName);
        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM).headers(getDefaultHeaders()).post(Entity.entity(new ByteArrayInputStream(data), MediaType.APPLICATION_OCTET_STREAM), Response.class);
        if (response.getStatus() / 100 != 2) {
            throw new Exception("Fail to install lua page: " + response.getStatus() + ".");
        }
        return true;
    }

    public List<DefaultFile> fileBySlbId(Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId required");
        WebTarget target = getTarget().path("/api/file/slb/list");
        target = target.queryParam("slbId", slbId);
        List<DefaultFile> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<List<DefaultFile>>() {
        });
        return result;
    }

    public DefaultFile fileByIpAndFileName(String ip, String fileName) throws Exception {
        if (ip == null || fileName == null) throw new ValidationException("IP and FileName is required");
        WebTarget target = getTarget().path("/api/file/ip/get");
        target = target.queryParam("ip", ip);
        target = target.queryParam("fileName", fileName);
        DefaultFile result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), DefaultFile.class);
        return result;
    }

    public ByteArrayOutputStream getFileData(String name, Long version) throws Exception {
        return getInstalledFile(this.getBaseUrl() + "/api/file/file/get?fileName="+name+"&version="+version);
    }


    // Install Session ticket file
    public boolean sessionTicketFile(byte[] data) throws Exception {
        if (data == null || data.length == 0) throw new ValidationException("Session ticket data shall not be empty");
        WebTarget target = getTarget().path("/api/session/ticket/key/install/local");
        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM).headers(getDefaultHeaders()).post(Entity.entity(new ByteArrayInputStream(data), MediaType.APPLICATION_OCTET_STREAM), Response.class);
        if (response.getStatus() / 100 != 2) {
            throw new Exception("Fail to install session ticket page: " + response.getStatus() + ".");
        }
        return true;
    }

    public HashMap<String, DefaultFile> sessionTicketDefaultFiles(Long slbId, String ip) throws Exception {
        if (slbId == null || ip == null) throw new ValidationException("SlbId and IP are required");
        WebTarget target = getTarget().path("/api/session/ticket/key/get/data/files");
        target = target.queryParam("slbId", slbId);
        target = target.queryParam("ip", ip);
        HashMap<String, DefaultFile> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, DefaultFile>>() {
        });

        return result;
    }

    public ByteArrayOutputStream sessionTicketFile(Long version) throws Exception {
        return getInstalledFile(this.getBaseUrl() + "/api/session/ticket/key/get/file?version="+version);
    }

    public String sessionTicketSetStatus(String ip, Long version) throws Exception {
        WebTarget target = getTarget().path("/api/session/ticket/key/set/status");
        target = target.queryParam("version", version);
        target = target.queryParam("ip", ip);
        HashMap<String, String> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, String>>() {
        });
        return result.get("message");
    }


    // Install Lua
    public boolean luaFile(String name, byte[] data) throws Exception {
        if (data == null || data.length == 0) throw new ValidationException("Lua data shall not be empty");
        WebTarget target = getTarget().path("/api/lua/install/local");
        target = target.queryParam("fileName", name);
        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM).headers(getDefaultHeaders()).post(Entity.entity(new ByteArrayInputStream(data), MediaType.APPLICATION_OCTET_STREAM), Response.class);
        if (response.getStatus() / 100 != 2) {
            throw new Exception("Fail to install lua page: " + response.getStatus() + ".");
        }
        return true;
    }

    public List<DefaultFile> luaDefaultFilesBySlbId(Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId required");
        WebTarget target = getTarget().path("/api/lua/slb/list");
        target = target.queryParam("slbId", slbId);
        List<DefaultFile> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<List<DefaultFile>>() {
        });
        return result;
    }

    public ByteArrayOutputStream getLuaFile(String name, Long version) throws Exception {
        return getInstalledFile(this.getBaseUrl() + "/api/lua/file/get?fileName=" + name+"&version="+version);
    }

    public DefaultFile luaDefaultFileByIp(String fileName, String ip) throws Exception {
        if (ip == null || fileName == null) throw new ValidationException("Ip and FileName required");
        WebTarget target = getTarget().path("/api/lua/ip/get");
        target = target.queryParam("ip", ip);
        target = target.queryParam("fileName", fileName);
        DefaultFile result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), DefaultFile.class);
        return result;
    }

    public String luaConfFile(Long slbId) throws Exception {
        WebTarget target = getTarget().path("/api/lua/conf/install/local");
        target = target.queryParam("slbId", slbId);
        HashMap<String, String> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, String>>() {
        });
        return result.get("message");
    }

    public String luaSetStatus(String ip, Long verion, String fileName) throws Exception {
        WebTarget target = getTarget().path("/api/lua/set/status");
        target = target.queryParam("version", verion);
        target = target.queryParam("ip", ip);
        target = target.queryParam("fileName", fileName);
        HashMap<String, String> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, String>>() {
        });
        return result.get("message");
    }

    // Install Error Page
    public boolean installErrorPage(String code, byte[] data) throws Exception {
        if (data == null || data.length == 0) throw new ValidationException("Error page data shall not be empty");
        WebTarget target = getTarget().path("/api/installErrorPage/new/install/local");
        target = target.queryParam("code", code);
        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM).headers(getDefaultHeaders()).post(Entity.entity(new ByteArrayInputStream(data), MediaType.APPLICATION_OCTET_STREAM), Response.class);
        if (response.getStatus() / 100 != 2) {
            throw new Exception("Fail to install installErrorPage page: " + response.getStatus() + ".");
        }

        return true;
    }

    public List<DefaultFile> errorPageBySlbId(Long slbId) throws Exception {
        if (slbId == null) throw new ValidationException("SlbId is required");
        WebTarget target = getTarget().path("/api/installErrorPage/slb/list");
        target = target.queryParam("slbId", slbId);
        List<DefaultFile> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<List<DefaultFile>>() {
        });
        return result;
    }

    public DefaultFile errorPageDefaultFileByIp(String code, String ip) throws Exception {
        if (ip == null || code == null) throw new ValidationException("Ip and code required");
        WebTarget target = getTarget().path("/api/installErrorPage/ip/get");
        target = target.queryParam("ip", ip);
        target = target.queryParam("code", code);
        DefaultFile result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), DefaultFile.class);
        return result;
    }

    public ByteArrayOutputStream getErrorPageFile(String code, Long version) throws Exception {
        if (code == null || version == null) throw new ValidationException("code and version are required");

        return getInstalledFile(this.getBaseUrl() + "/api/installErrorPage/file/get?code=" + code + "&version=" + version);
    }

    public String errorPageSetStatus(String ip, Long version, String code) throws Exception {
        WebTarget target = getTarget().path("/api/installErrorPage/set/status");
        target = target.queryParam("version", version);
        target = target.queryParam("ip", ip);
        target = target.queryParam("code", code);
        HashMap<String, String> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, String>>() {
        });
        return result.get("message");
    }

    // Install index page
    public boolean indexPage(Long version, byte[] data) throws Exception {
        if (data == null || data.length == 0) throw new ValidationException("Index page data shall not be empty");
        WebTarget target = getTarget().path("/api/indexPage/install/local");
        target = target.queryParam("version", version);
        Response response = target.request(MediaType.APPLICATION_OCTET_STREAM).headers(getDefaultHeaders()).post(Entity.entity(new ByteArrayInputStream(data), MediaType.APPLICATION_OCTET_STREAM), Response.class);
        if (response.getStatus() / 100 != 2) {
            throw new Exception("Fail to install index page: " + response.getStatus() + ".");
        }
        return true;
    }

    public String indexPageSetStatus(String ip, Long version) throws Exception {
        WebTarget target = getTarget().path("/api/indexPage/set/status");
        target = target.queryParam("version", version);
        target = target.queryParam("ip", ip);

        return target.request().headers(getDefaultHeaders()).get(String.class);
    }

    public List<DefaultFile> indexPageListFiles(Long slbId) throws Exception {
        WebTarget target = getTarget().path("/api/indexPage/list/current/files");
        target = target.queryParam("slbId", slbId);

        List<DefaultFile> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<List<DefaultFile>>() {
        });

        return result;
    }

    public DefaultFile indexPageFile(String fileName, String ip) throws Exception {
        WebTarget target = getTarget().path("/api/indexPage/get/current/file");
        target = target.queryParam("fileName", fileName);
        target = target.queryParam("ip", ip);
        DefaultFile result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), DefaultFile.class);
        return result;
    }

    public ByteArrayOutputStream indexPageFile(Long version) throws Exception {
        return getInstalledFile(this.getBaseUrl() + "/api/indexPage/file/get?version=" + version);
    }

    public String fileSetStatus(String ip, Long version, String fileName) throws Exception {
        WebTarget target = getTarget().path("/api/file/set/status");
        target = target.queryParam("version", version);
        target = target.queryParam("ip", ip);
        target = target.queryParam("fileName", fileName);

        HashMap<String, String> result = ObjectJsonParser.parse(target.request().headers(getDefaultHeaders()).get(String.class), new com.fasterxml.jackson.core.type.TypeReference<HashMap<String, String>>() {
        });
        return result.get("message");
    }

    // Common methods
    private ByteArrayOutputStream getInstalledFile(String path) throws Exception {
        URLConnection conn;
        HttpURLConnection httpURLConnection;
        URL url = new URL(path);
        conn = url.openConnection();

        httpURLConnection = (HttpURLConnection) conn;
        httpURLConnection.setDoOutput(false);
        httpURLConnection.setUseCaches(false);

        httpURLConnection.setRequestMethod("GET");
        int responseCode = httpURLConnection.getResponseCode();
        if (responseCode == 200) {
            return consumeStream(httpURLConnection.getInputStream());
        }
        return null;
    }

    private ByteArrayOutputStream consumeStream(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf;
    }

}
