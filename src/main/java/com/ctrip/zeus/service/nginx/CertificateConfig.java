package com.ctrip.zeus.service.nginx;

/**
 * Created by zhoumy on 2015/10/30.
 */
public class CertificateConfig {
    public static final boolean OVERWRITE_IF_EXIST = false;
    public static final boolean APPEND_ONLY = true;

    private String cacheDir;
    private boolean writeFileOption;

    public CertificateConfig() {
        cacheDir = "tmp/certs/";
        writeFileOption = OVERWRITE_IF_EXIST;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        if (!cacheDir.endsWith("/"))
            cacheDir += "/";
        this.cacheDir = cacheDir;
    }

    public boolean getWriteFileOption() {
        return writeFileOption;
    }

    public void setWriteFileOption(boolean writeFileOption) {
        this.writeFileOption = writeFileOption;
    }
}