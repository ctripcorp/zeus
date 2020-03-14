package com.ctrip.zeus.restful.resource.tools;

import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.model.model.ServerWarInfo;
import com.ctrip.zeus.model.tools.CheckResponse;
import com.ctrip.zeus.model.tools.CheckSlbreleaseResponse;
import com.ctrip.zeus.model.tools.CheckTarget;
import com.ctrip.zeus.model.tools.Header;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Created by ygshen on 2016/11/30.
 */
public class CheckerClient extends AbstractRestClient {
    private final static Logger logger = LoggerFactory.getLogger(CheckerClient.class);

    protected CheckerClient(String url, int readTimeout) {
        super(url, readTimeout);
    }

    public static CheckerClient getInstance(String url, int timeout) {
        return new CheckerClient(url, timeout);
    }

    public CheckResponse check(String uri, CheckTarget target) {
        long startTime = 0;
        long endTime = 0;
        int code = 0;
        String status;
        String hostIP = null;

        String domain = target.getHost();
        String agent = target.getAgent();
        Set<Header> headers = new HashSet<>(target.getHeaders());

        try {
            Map<String, String> requestedHeaderMap = new HashMap<>();
            if (headers != null && headers.size() > 0) {
                for (Header h : headers) {
                    if (!Strings.isNullOrEmpty(h.getKey())) {
                        requestedHeaderMap.put(h.getKey().toLowerCase(), h.getValue());
                    }
                }
            }
            if (domain != null && !domain.isEmpty()) {
                if (!requestedHeaderMap.containsKey("host")) {
                    requestedHeaderMap.put("HOST", domain);
                }
            }
            if (agent != null && !agent.isEmpty()) {
                if (!requestedHeaderMap.containsKey("user-agent")) {
                    requestedHeaderMap.put("User-Agent", agent);
                }
            }
            startTime = System.currentTimeMillis();

            // res = getTarget().path(uri).request().headers(header).get();
            Map<String, String> response = request(uri, "GET", "", requestedHeaderMap, "");
            endTime = System.currentTimeMillis();

            String codeString = response.get("code");
            code = Integer.parseInt(codeString);
            status = Response.Status.fromStatusCode(code).toString();
            hostIP = response.get("host");
        } catch (Exception ex) {
            status = ex.getMessage();
            endTime = System.currentTimeMillis();
        }

        CheckResponse response = new CheckResponse();
        response.setCode(code);
        response.setTime(endTime - startTime);
        response.setStatus(status);
        response.setHostIp(hostIP);
        return response;
    }

    public CheckSlbreleaseResponse checkSlbReleaseInfo(String uri) {
        int code = -1;
        Response res = null;
        ServerWarInfo commit = null;

        String status = "None";
        try {
            res = getTarget().path(uri).request().get();
            code = res.getStatus();
            if (code == 200) {
                String responseText = res.readEntity(String.class);
                commit = ObjectJsonParser.parse(responseText, ServerWarInfo.class);
            }

            status = Response.Status.fromStatusCode(code).toString();

        } catch (Exception ex) {
            status = ex.getMessage();
        }

        CheckSlbreleaseResponse response = new CheckSlbreleaseResponse();

        String commitId = "";
        if (commit != null) {
            commitId = commit.getCommitId();
        }

        response.setCommitId(commitId);

        response.setCode(code);
        response.setStatus(status);

        return response;
    }

    public static String CheckDomainVpn(String domain) {
        String hostIp = "";

        if (!domain.isEmpty()) {
            try {
                InetAddress inetAddress = InetAddress.getByName(domain);
                hostIp = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                logger.warn(String.format("[Check Target Url]: Failed to resolve target host: {0}, while checking url", domain));
            }
        }
        return hostIp;
    }

    public static CheckResponse visit(String url, String proxy, Map<String, String> headers) {
        // Status and code
        int code = -1;
        String status = "None";
        // Response body text as plain text
        String plainText = "";
        String groupId = "";
        String env = "";
        // Host and Host address ip
        String host = "";
        String hostIp = "";
        try {
            Map<String, String> result = request(url, "GET", proxy, headers, null);

            URL urlData = new URL(url);
            host = urlData.getHost();

            code = Integer.parseInt(result.get("code"));
            if (code != 0) {
                status = Response.Status.fromStatusCode(code).toString();
            }
            plainText = result.get("response");
            if (plainText == null || plainText.isEmpty() || !plainText.contains("GroupId=")) {
                groupId = "-1";
            } else {
                String[] retArray = plainText.split(";");
                if (retArray.length == 2) {
                    String groupIdString = retArray[0];
                    String envString = retArray[1];

                    String[] groupIdArray = groupIdString.split("=");
                    if (groupIdArray.length == 2) {
                        groupId = groupIdString.split("=")[1];
                    }

                    String[] envArray = envString.split("=");
                    if (envArray.length == 2) {
                        env = envArray[1];
                    }
                } else {
                    groupId = "-1";
                    env = "-1";
                }
            }
        } catch (Exception ex) {
            logger.warn(String.format("[Check Target Url]: Failed to get any response while checking url:{0}, With error message: {1}",
                    url, ex.getMessage()));
            status = ex.getMessage();
        }

        if (!host.isEmpty()) {
            hostIp = CheckDomainVpn(host);
        }

        CheckResponse response = new CheckResponse();
        response.setHostIp(hostIp);
        response.setCode(code);
        response.setStatus(status);
        response.setGroup(groupId);
        response.setEnv(env);
        return response;
    }

    public static Map<String, String> visit(String method, String url, Map<String, String> params, Map<String, String> headers, String cookie, String bodyText, String proxy) throws IOException {
        Map<String, String> res;

        try {
            Map<String, String> header = new HashMap<>();
            header.put("Cookie", cookie);


            // Url reformat
            String urlStr = url;
            ArrayList segments = new ArrayList();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                segments.add(entry.getKey() + "=" + entry.getValue());
            }
            if (segments.size() > 0) {
                urlStr = url + "?" + Joiner.on("&").join(segments);
            }

            // request headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                header.put(entry.getKey(), entry.getValue());
            }
            res = request(urlStr, method, proxy, header, bodyText);
        } catch (Exception ex) {
            logger.warn("Check Url Failed.URL:" + url);
            throw ex;
        }
        return res;
    }

    private static Map<String, String> request(String urlStr, String method, String proxy,
                                               Map<String, String> headers, String bodyText) throws IOException {
        Map<String, String> result = new HashMap<>();
        URLConnection conn;
        HttpURLConnection httpURLConnection = null;
        String response = "";
        int code = 0;
        try {
            supportSSLDefaultCert();
            URL url = new URL(urlStr);
            result.put("host", url.getHost());
            if (proxy != null) {
                String[] tmp = proxy.split(":");
                if (tmp.length >= 2) {
                    Proxy p = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(tmp[0], Integer.parseInt(tmp[1])));
                    conn = url.openConnection(p);
                } else {
                    conn = url.openConnection();
                }
            } else {
                conn = url.openConnection();
            }
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            httpURLConnection = (HttpURLConnection) conn;
            if (method.equalsIgnoreCase("post") || method.equalsIgnoreCase("put")) {
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod(method.toUpperCase());

                for (String key : headers.keySet()) {
                    httpURLConnection.setRequestProperty(key, headers.get(key));
                }
                OutputStream outStrm = httpURLConnection.getOutputStream();
                OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStrm);
                try {
                    outStreamWriter.write(bodyText);
                    outStreamWriter.flush();
                } finally {
                    outStreamWriter.close();
                }

            } else {
                httpURLConnection.setDoOutput(false);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestMethod(method.toUpperCase());

                for (String key : headers.keySet()) {
                    httpURLConnection.setRequestProperty(key, headers.get(key));
                }
            }
            code = httpURLConnection.getResponseCode();
            InputStream inputStream = httpURLConnection.getInputStream();
            response = consumeStream(inputStream);
            inputStream.close();
        } catch (Exception e) {
            response = "Connection Failed. Error Code: " + code + ".\n Error Message:" + e.getMessage();
            logger.warn(response, e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        result.put("code", String.valueOf(code));
        result.put("response", response);
        return result;
    }

    private static String consumeStream(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    private static void supportSSLDefaultCert() throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    }
}
