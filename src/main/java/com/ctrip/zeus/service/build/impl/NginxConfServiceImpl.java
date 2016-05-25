package com.ctrip.zeus.service.build.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.nginx.entity.*;
import com.ctrip.zeus.nginx.transform.DefaultJsonParser;
import com.ctrip.zeus.service.build.BuildInfoService;
import com.ctrip.zeus.service.build.NginxConfService;
import com.ctrip.zeus.service.version.ConfVersionService;
import com.ctrip.zeus.util.CompressUtils;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author:xingchaowang
 * @date: 3/15/2015.
 */
@Service("nginxConfService")
public class NginxConfServiceImpl implements NginxConfService {
    @Resource
    private NginxConfDao nginxConfDao;
    @Resource
    private NginxConfSlbDao nginxConfSlbDao;
    @Resource
    private ConfVersionService confVersionService;

    private Logger logger = LoggerFactory.getLogger(NginxConfServiceImpl.class);

    @Override
    public String getNginxConf(Long slbId, Long version) throws Exception {
        NginxConfDo nginxConfDo = nginxConfDao.findBySlbIdAndVersion(slbId, (int) version.longValue(), NginxConfEntity.READSET_FULL);
        if (nginxConfDo == null) {
            throw new ValidationException("Not found nginx conf by slbId and version. slbId:" + slbId + " version:" + version);
        }
        return nginxConfDo.getContent();
    }

    @Override
    public NginxConfEntry getUpstreamsAndVhosts(Long slbId, Long version) throws Exception {
        NginxConfSlbDo d = nginxConfSlbDao.findBySlbAndVersion(slbId, version, NginxConfSlbEntity.READSET_FULL);
        if (d == null) return null;

        NginxConfEntry entry = DefaultJsonParser.parse(NginxConfEntry.class, CompressUtils.decompress(d.getContent()));
        if (entry.getUpstreams().getFiles().size() == 0 || entry.getVhosts().getFiles().size() == 0) {
            logger.warn("No vhost or upstream files exists. "
                    + "Upstream size: " + entry.getUpstreams().getFiles().size()
                    + ", Vhost size: " + entry.getVhosts().getFiles().size() + ".");
        }
        return entry;
    }

    @Override
    public NginxConfEntry getUpstreamsAndVhosts(Long slbId, Long version, List<Long> vsIds) throws Exception {
        NginxConfEntry entry = new NginxConfEntry().setUpstreams(new Upstreams()).setVhosts(new Vhosts());
        NginxConfEntry completeEntry = getUpstreamsAndVhosts(slbId, version);
        if (completeEntry == null) return null;

        Set<Long> origin = new HashSet<>(vsIds);
        Set<Long> vhostIdCheck = new HashSet<>();
        Set<Long> upstreamIdCheck = new HashSet<>();

        for (ConfFile cf : completeEntry.getVhosts().getFiles()) {
            try {
                Long vsId = Long.parseLong(cf.getName());
                if (origin.contains(vsId)) {
                    entry.getVhosts().addConfFile(cf);
                    if (!vhostIdCheck.contains(vsId)) vhostIdCheck.add(vsId);
                }
            } catch (NumberFormatException ex) {
                logger.error("Unable to extract vs id information from vhost file: " + cf.getName() + ".");
            }
        }
        for (ConfFile cf : completeEntry.getUpstreams().getFiles()) {
            String[] fn = cf.getName().split("_");
            boolean add = false;
            for (String relatedVsId : fn) {
                if (relatedVsId.isEmpty()) continue;

                Long vsId = 0L;
                try {
                    vsId = Long.parseLong(relatedVsId);
                } catch (NumberFormatException ex) {
                    logger.warn("Unable to extract vs id information from upstream file: " + cf.getName() + ".");
                    break;
                }
                if (vsIds.contains(vsId)) {
                    if (!add) {
                        add = true;
                        if (!upstreamIdCheck.contains(vsId)) upstreamIdCheck.add(vsId);
                    }
                }
            }
            if (add) {
                entry.getUpstreams().addConfFile(cf);
            }
        }

        vhostIdCheck.removeAll(origin);
        upstreamIdCheck.removeAll(origin);
        if (vhostIdCheck.isEmpty() && upstreamIdCheck.isEmpty()) {
            return entry;
        } else {
            StringBuilder err = new StringBuilder();
            err.append("Unexpected missing vhost conf files of given vs ids: " + Joiner.on(",").join(vhostIdCheck)).append('.').append('\n');
            err.append("Unexpected missing upstream conf files of given vs ids: " + Joiner.on(",").join(upstreamIdCheck)).append('.').append('\n');
            logger.error(err.toString());
            throw new ValidationException(err.toString());
        }
    }

    @Override
    public int getCurrentVersion(Long slbId) throws Exception {
        return confVersionService.getSlbCurrentVersion(slbId).intValue();
    }

}
