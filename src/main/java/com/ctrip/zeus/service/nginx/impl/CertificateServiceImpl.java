package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.util.IOUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

/**
 * Created by zhoumy on 2015/10/29.
 */
@Service("certificateService")
public class CertificateServiceImpl implements CertificateService {
    private final static String TempDir = "tmp/certs/";

    @Override
    public void cache(InputStream cert, InputStream key, Long vsId) throws Exception {
        cache(cert, key, TempDir + vsId);
    }

    @Override
    public void cache(InputStream cert, InputStream key, String dir) throws Exception {
        File f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        OutputStream certos = new FileOutputStream(f.getPath() + "/ssl.crt");
        IOUtils.copy(cert, certos);
        certos.flush();
        OutputStream keyos = new FileOutputStream(f.getPath()+"/ssl.key");
        IOUtils.copy(key, keyos);
        certos.flush();
    }

    @Override
    public void sendIfExist(Long vsId, List<String> ip) {

    }
}
