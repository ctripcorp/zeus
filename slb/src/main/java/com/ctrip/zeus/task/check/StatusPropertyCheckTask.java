package com.ctrip.zeus.task.check;

import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.*;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.task.AbstractTask;
import com.ctrip.zeus.util.EnvHelper;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;

@Service
public class StatusPropertyCheckTask extends AbstractTask {
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private DrCriteriaQuery drCriteriaQuery;
    @Resource
    private TrafficPolicyQuery trafficPolicyQuery;
    @Resource
    private PropertyBox propertyBox;


    private static DynamicIntProperty interval = DynamicPropertyFactory.getInstance().getIntProperty("status.property.check.interval", 10 * 60000);
    private static final DynamicBooleanProperty enable = DynamicPropertyFactory.getInstance().getBooleanProperty("status.property.check.enable", false);
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        if (!EnvHelper.portal() || !enable.get()) {
            return;
        }
        syncSlb();
        syncVs();
        syncGroup();
    }

    private void syncGroup() throws Exception {
        Set<Long> groupIds = groupCriteriaQuery.queryAll();
        Set<Long> tobeActIds = new HashSet<>();
        Set<Long> actIds = new HashSet<>();
        Set<Long> deactIds = new HashSet<>();
        groupIds.forEach(id -> {
            try {
                IdVersion[] idv = virtualServerCriteriaQuery.queryByIdAndMode(id, SelectionMode.REDUNDANT);
                if (idv.length == 2) {
                    if (!idv[0].getVersion().equals(idv[1].getVersion())) {
                        tobeActIds.add(id);
                    } else {
                        actIds.add(id);
                    }
                } else if (idv.length == 1) {
                    deactIds.add(id);
                }
            } catch (Exception e) {
                logger.warn("Get Group Versions Failed.");
            }
        });
        if (actIds.size() > 0) {
            propertyBox.set("status", "activated", "group", actIds.toArray(new Long[actIds.size()]));
        }
        if (tobeActIds.size() > 0) {
            propertyBox.set("status", "toBeActivated", "group", tobeActIds.toArray(new Long[tobeActIds.size()]));
        }
        if (deactIds.size() > 0) {
            propertyBox.set("status", "deactivated", "group", deactIds.toArray(new Long[deactIds.size()]));
        }
    }

    private void syncVs() throws Exception {
        Set<Long> vsIds = virtualServerCriteriaQuery.queryAll();
        Set<Long> tobeActIds = new HashSet<>();
        Set<Long> actIds = new HashSet<>();
        Set<Long> deactIds = new HashSet<>();
        vsIds.forEach(id -> {
            try {
                IdVersion[] idv = virtualServerCriteriaQuery.queryByIdAndMode(id, SelectionMode.REDUNDANT);
                if (idv.length == 2) {
                    if (!idv[0].getVersion().equals(idv[1].getVersion())) {
                        tobeActIds.add(id);
                    } else {
                        actIds.add(id);
                    }
                } else if (idv.length == 1) {
                    deactIds.add(id);
                }
            } catch (Exception e) {
                logger.warn("Get Vs Versions Failed.");
            }
        });
        if (actIds.size() > 0) {
            propertyBox.set("status", "activated", "vs", actIds.toArray(new Long[actIds.size()]));
        }
        if (tobeActIds.size() > 0) {
            propertyBox.set("status", "toBeActivated", "vs", tobeActIds.toArray(new Long[tobeActIds.size()]));
        }
        if (deactIds.size() > 0) {
            propertyBox.set("status", "deactivated", "vs", deactIds.toArray(new Long[deactIds.size()]));
        }
    }

    private void syncSlb() throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        Set<Long> tobeActSlbIds = new HashSet<>();
        Set<Long> actSlbIds = new HashSet<>();
        Set<Long> deactSlbIds = new HashSet<>();
        slbIds.forEach(slbId -> {
            try {
                IdVersion[] idv = slbCriteriaQuery.queryByIdAndMode(slbId, SelectionMode.REDUNDANT);
                if (idv.length == 2) {
                    if (!idv[0].getVersion().equals(idv[1].getVersion())) {
                        tobeActSlbIds.add(slbId);
                    } else {
                        actSlbIds.add(slbId);
                    }
                } else if (idv.length == 1) {
                    deactSlbIds.add(slbId);
                }
            } catch (Exception e) {
                logger.warn("Get Slb Versions Failed.");
            }
        });
        if (actSlbIds.size() > 0) {
            propertyBox.set("status", "activated", "slb", actSlbIds.toArray(new Long[actSlbIds.size()]));
        }
        if (tobeActSlbIds.size() > 0) {
            propertyBox.set("status", "toBeActivated", "slb", tobeActSlbIds.toArray(new Long[tobeActSlbIds.size()]));
        }
        if (deactSlbIds.size() > 0) {
            propertyBox.set("status", "deactivated", "slb", deactSlbIds.toArray(new Long[deactSlbIds.size()]));
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return interval.get();
    }

}