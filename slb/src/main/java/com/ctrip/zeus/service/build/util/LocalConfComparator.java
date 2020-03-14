package com.ctrip.zeus.service.build.util;

import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.util.IOUtils;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Discription
 **/
@Component("localConfComparator")
public class LocalConfComparator {

    private final DynamicStringProperty NGINX_CONF_DIR = DynamicPropertyFactory.getInstance().getStringProperty("nginx.conf.dir", "/opt/app/nginx/conf");

    private final String CONF_SUFFIX = ".conf";

    private final Logger logger = LoggerFactory.getLogger(LocalConfComparator.class);

    public boolean isNginxConfSame(String confContent) {
        try {
            String confPath = NGINX_CONF_DIR.get() + File.separator + "nginx.conf";
            File file = new File(confPath);
            if (file.exists() && file.isFile()) {
                String localConf = readFiletoString(confPath);
                if (confContent != null) {
                    return confContent.equals(localConf);
                }
            }
        } catch (IOException e) {
            logger.error("IOException happens when reading local nginx.conf. Message: " + e.getMessage());
        }
        return false;
    }

    public boolean isVhostConfsSame(NginxConfEntry nxEntry) {
        /*
         * @Description
         * @parameter: entry: Only contains incremental vs conf and upstream conf
         * @return true if all vhost confs specified in entry exists in local vhosts directory and have same contents, return false otherwise
         **/
        if (nxEntry != null) {
            Map<String, String> nextConfMap = new HashMap<>();
            for (ConfFile confFile : nxEntry.getVhosts().getFiles()) {
                if (confFile != null) {
                    nextConfMap.put(confFile.getName() + CONF_SUFFIX, confFile.getContent());
                }
            }
            try {
                String vhostDir = NGINX_CONF_DIR.get() + File.separator + "vhosts";
                if (!isDirExists(vhostDir)) {
                    return false;
                }
                String[] localVHhost = getFileList(vhostDir);
                Set<String> localVHostSet = new HashSet<>(Arrays.asList(localVHhost));
                for (String nextVhostFile : nextConfMap.keySet()) {
                    if (!localVHostSet.contains(nextVhostFile)) {
                        logger.info("No vhost conf in disk: " + nextVhostFile);
                        logger.info("local disk: " + localVHostSet);
                        return false;
                    }

                    String localContent = readFiletoString(vhostDir + File.separator + nextVhostFile);
                    if (!localContent.equals(nextConfMap.get(nextVhostFile))) {
                        logger.info("vhost confs differ. ");
                        logger.info("local: " + localContent);
                        logger.info("next: " + nextConfMap.get(nextVhostFile));
                        return false;
                    }
                }
            } catch (IOException e) {
                logger.error("IOException happens when reading vhosts conf file. Message: " + e.getMessage());
            }
        }
        return true;
    }

    private String[] getFileList(String path) {
        File file = new File(path);
        String[] res = file.list();
        if (res != null) {
            return res;
        } else {
            return new String[0];
        }
    }

    public String readFiletoString(String filePath) throws IOException {
        // File's existence has been checked
        InputStream is = null;
        try {
            is = new FileInputStream(new File(filePath));
            return IOUtils.inputStreamStringify(is);
        } catch (IOException e) {
            logger.error("Fail to read local conf file");
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private boolean isDirExists(final String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }
}
