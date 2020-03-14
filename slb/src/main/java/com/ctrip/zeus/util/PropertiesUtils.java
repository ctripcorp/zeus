package com.ctrip.zeus.util;

import org.springframework.util.CollectionUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Properties;

public class PropertiesUtils {

    public static void updatePropertiesFileOnDisk(String path, Map<String, String> incremental) throws Exception {
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            Properties originalFromDisk = new Properties();
            fis = new FileInputStream(path);
            originalFromDisk.load(fis);

            if (!CollectionUtils.isEmpty(incremental)) {
                for (Map.Entry<String, String> kv : incremental.entrySet()) {
                    if (originalFromDisk.containsKey(kv.getKey())) {
                        originalFromDisk.setProperty(kv.getKey(), kv.getValue());
                    }
                }
            }

            fos = new FileOutputStream(path);
            originalFromDisk.store(fos, null);
        } finally {
            if (fis != null) {
                fis.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
}
