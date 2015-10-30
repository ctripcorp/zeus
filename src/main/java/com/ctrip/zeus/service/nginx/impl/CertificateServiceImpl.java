package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.client.AbstractRestClient;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.nginx.CertificateConfig;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.util.IOUtils;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("certificateService")
public class CertificateServiceImpl implements CertificateService {
    private CertificateConfig config = new CertificateConfig();

    @Override
    public CertificateConfig getConfig() {
        return config;
    }

    @Override
    public void cache(InputStream cert, InputStream key, Long vsId) throws Exception {
        save(cert, key, config.getCacheDir() + vsId);
    }

    @Override
    public void save(InputStream cert, InputStream key, String dir) throws Exception {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        OutputStream certos = new FileOutputStream(f.getPath() + "/ssl.crt", config.getWriteFileOption());
        OutputStream keyos = new FileOutputStream(f.getPath() + "/ssl.key", config.getWriteFileOption());
        try {
            IOUtils.copy(cert, certos);
            certos.flush();
            IOUtils.copy(key, keyos);
            certos.flush();
        } finally {
            certos.close();
            keyos.close();
        }
    }

    @Override
    public void sendIfExist(Long vsId, List<String> ips) throws Exception {
        String errMsg = "";
        boolean success = true;
        for (String ip : ips) {
            CertSyncClient c = new CertSyncClient("http://" + ip + ":8099/api/op/installcerts", config.getCacheDir() + vsId);
            Response res = c.sync(vsId);
            // retry
            if (res.getStatus() / 100 > 2)
                res = c.sync(vsId);
            // still failed after retry
            if (res.getStatus() / 100 > 2) {
                success &= false;
                try {
                    errMsg += ip + ":" + IOUtils.inputStreamStringify((InputStream) res.getEntity()) + ";";
                } catch (IOException e) {
                    errMsg += ip + ":" + "Unable to parse response entity.";
                }
            }
        }
        if (success)
            return;
        throw new Exception(errMsg);
    }

    private static class CertSyncClient extends AbstractRestClient {
        private final String fileDir;

        protected CertSyncClient(String url, String fileDir) {
            super(url);
            this.fileDir = fileDir;
        }

        public Response sync(Long vsId) throws ValidationException {
            File cert = new File(fileDir + "/ssl.crt");
            File key = new File(fileDir + "/ssl.key");
            if (cert.exists() && key.exists()) {
                MultiPart mp = new MultiPart().bodyPart(new FileDataBodyPart("cert", cert, MediaType.APPLICATION_OCTET_STREAM_TYPE))
                        .bodyPart(new FileDataBodyPart("key", key, MediaType.APPLICATION_OCTET_STREAM_TYPE));
                return getTarget().queryParam("vsId", vsId).request(MediaType.MULTIPART_FORM_DATA).post(
                        Entity.entity(mp, mp.getMediaType()));
            }
            throw new ValidationException("Certificate files cannot be found under " + fileDir);
        }
    }
}
