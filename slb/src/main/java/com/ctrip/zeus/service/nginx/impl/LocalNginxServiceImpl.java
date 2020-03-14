package com.ctrip.zeus.service.nginx.impl;

import com.ctrip.zeus.service.nginx.LocalNginxService;
import com.ctrip.zeus.util.IOUtils;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Discription
 **/
@Service("localNginxService")
public class LocalNginxServiceImpl implements LocalNginxService {

    private final DynamicStringProperty DEFAULT_NGINX_CONF_DIR = DynamicPropertyFactory.getInstance().getStringProperty("default.nginx.conf.dir", "/opt/app/nginx/conf");
    private final DynamicStringProperty NGINX_CONF_FILE = DynamicPropertyFactory.getInstance().getStringProperty("default.nginx.conf.file.name", "nginx.conf");
    private final DynamicStringProperty VHOSTS_DIR = DynamicPropertyFactory.getInstance().getStringProperty("default.vhosts.dir", "vhosts");
    private final DynamicStringProperty UPSTREAMS_DIR = DynamicPropertyFactory.getInstance().getStringProperty("default.upstreams.dir", "upstreams");

    private final Logger logger = LoggerFactory.getLogger(LocalNginxServiceImpl.class);

    @Override
    public String getNginxConf() throws Exception {
        File file = new File(DEFAULT_NGINX_CONF_DIR.get(), NGINX_CONF_FILE.get());
        if (file.exists() && file.isFile()) {
            return IOUtils.inputStreamStringify(new FileInputStream(file));
        }
        logger.error("Unable to find nginx.conf in local disk. ");
        return null;
    }

    @Override
    public String getVsConf(Long vsId) throws Exception {
        if (vsId != null && vsId > 0) {
            String targetFileName = "" + vsId + ".conf";
            File file = new File(DEFAULT_NGINX_CONF_DIR.get(), VHOSTS_DIR.get());
            if (!file.exists() || !file.isDirectory()) {
                logger.warn("vhosts directory not exists");
            } else {
                String[] candidates = file.list();
                if (candidates != null) {
                    for (String candidate : candidates) {
                        if (targetFileName.equalsIgnoreCase(candidate)) {
                            return IOUtils.inputStreamStringify(new FileInputStream(new File(file, candidate)));
                        }
                    }
                }
            }
        }
        logger.warn("vs's conf not found. Vsid: " + vsId);
        return null;
    }

    @Override
    public Map<String, String> getUpstreamConfs(Long vsId) throws Exception {
        Map<String, String> fileContents = new HashMap<>();
        File upstreamsDir = new File(DEFAULT_NGINX_CONF_DIR.get(), UPSTREAMS_DIR.get());
        if (!upstreamsDir.exists() || !upstreamsDir.isDirectory()) {
            logger.warn("upstream directory not exists");
        } else {
            String[] candidates = upstreamsDir.list();
            if (candidates != null) {
                for (String candidate : candidates) {
                    if (!candidate.endsWith(".conf")) {
                        continue;
                    }
                    if (isVsIdInConfName(vsId, candidate)) {
                        String content = IOUtils.inputStreamStringify(new FileInputStream(new File(upstreamsDir, candidate)));
                        fileContents.put(candidate, content);
                    }
                }
            }

        }
        return fileContents;
    }

    @Override
    public Set<Long> getAllVsIds() {
        Set<Long> vsIds = new HashSet<>();
        File vhostsDir = new File(DEFAULT_NGINX_CONF_DIR.get(), VHOSTS_DIR.get());
        if (vhostsDir.exists() && vhostsDir.isDirectory()) {
            String[] candidates = vhostsDir.list();
            if (candidates != null) {
                for (String candidate : candidates) {
                    if (!candidate.endsWith(".conf")) {
                        continue;
                    }
                    int pos = candidate.indexOf(".conf");
                    try {
                        vsIds.add(Long.parseLong(candidate.substring(0, pos)));
                    } catch (NumberFormatException e) {
                        logger.warn("Cannot convert file name into vsid. Name: " + candidate.substring(0, pos));
                    }
                }
            }
        }
        return vsIds;
    }

    public boolean isVsIdInConfName(Long vsId, String name) {
        if (name != null) {
            if (!name.contains(".conf")) {
                return false;
            }
            String suffixRemoved = name.substring(0, name.indexOf(".conf"));
            String[] vsIds = suffixRemoved.split("_");
            String target = String.valueOf(vsId);

            for (String relatedVsId : vsIds) {
                if (target.equalsIgnoreCase(relatedVsId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
