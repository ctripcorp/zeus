package com.ctrip.zeus.service.nginx;

/**
 * Created by zhoumy on 2015/10/30.
 */
public class CertificateConfig {
    private static final boolean OVERWRITE_IF_EXIST = false;
    public static final boolean GRAYSCALE = false;
    public static final boolean ONBOARD = true;

    private String installDir;
    private boolean writeFileOption;

    public CertificateConfig() {
        installDir = "/data/nginx/ssl/";
        writeFileOption = OVERWRITE_IF_EXIST;
    }

    public String getInstallDir(Long vsId) {
        return installDir + vsId;
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }

    public boolean getWriteFileOption() {
        return writeFileOption;
    }
    
    public void setWriteFileOption(boolean writeFileOption) {
        this.writeFileOption = writeFileOption;
    }

}