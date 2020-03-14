package com.ctrip.zeus.service.model.handler.impl;

import com.ctrip.zeus.dao.entity.*;
import com.ctrip.zeus.dao.mapper.SlbArchiveSlbMapper;
import com.ctrip.zeus.dao.mapper.SlbSlbMapper;
import com.ctrip.zeus.dao.mapper.SlbSlbStatusRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.Slb;
import com.ctrip.zeus.service.model.VersionUtils;
import com.ctrip.zeus.service.model.handler.SlbSync;
import com.ctrip.zeus.support.ObjectJsonWriter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by zhoumy on 2015/9/29.
 */
@Component("slbEntityManager")
public class SlbEntityManager implements SlbSync {
    @Resource
    private SlbSlbMapper slbSlbMapper;

    @Resource
    private SlbArchiveSlbMapper slbArchiveSlbMapper;

    @Resource
    private SlbSlbServerRMaintainer slbSlbServerRMaintainer;

    @Resource
    private SlbSlbStatusRMapper slbSlbStatusRMapper;

    @Override
    public void add(Slb slb) throws Exception {
        slb.setVersion(1);
        SlbSlb record = parseSlb(slb.getId(), slb);
        if (slb.getId() == null || slb.getId().equals(0L)) {
            slbSlbMapper.insert(record);
            slb.setId(record.getId());
        } else {
            slbSlbMapper.insertIncludeId(record);
        }

        slbArchiveSlbMapper.insert(SlbArchiveSlb.builder().
                slbId(slb.getId()).
                createdTime(new Date()).
                version(slb.getVersion()).
                hash(VersionUtils.getHash(slb.getId(), slb.getVersion())).
                content(ContentWriters.writeSlbContent(slb)).
                build());

        SlbSlbStatusR target = SlbSlbStatusR.builder().
                slbId(slb.getId())
                .offlineVersion(slb.getVersion()).
                        onlineVersion(0).
                        build();
        slbSlbStatusRMapper.insertOrUpdate(target);
        slbSlbServerRMaintainer.insert(slb);
    }

    @Override
    public void update(Slb slb) throws Exception {
        SlbSlbStatusR check = slbSlbStatusRMapper.selectOneByExample(new SlbSlbStatusRExample().createCriteria().andSlbIdEqualTo(slb.getId()).example());
        if (check.getOfflineVersion() > slb.getVersion()) {
            throw new ValidationException("Newer slb version is detected.");
        }

        if (!check.getOfflineVersion().equals(slb.getVersion())) {
            throw new ValidationException("Incompatible slb version.");
        }

        slb.setVersion(slb.getVersion() + 1);
        slbSlbMapper.updateByPrimaryKeyWithBLOBs(parseSlb(slb.getId(), slb));

        slbArchiveSlbMapper.insert(SlbArchiveSlb.builder().
                slbId(slb.getId()).
                createdTime(new Date()).
                version(slb.getVersion()).
                hash(VersionUtils.getHash(slb.getId(), slb.getVersion())).
                content(ContentWriters.writeSlbContent(slb)).
                build());

        slbSlbServerRMaintainer.refreshOffline(slb);

        check.setOfflineVersion(slb.getVersion());
        slbSlbStatusRMapper.insertOrUpdate(check);
    }

    @Override
    public void updateStatus(List<Slb> slbs) throws Exception {
        if (slbs.size() == 0) return;
        Long[] slbIds = new Long[slbs.size()];

        SlbSlbStatusR[] dos = new SlbSlbStatusR[slbs.size()];
        for (int i = 0; i < dos.length; i++) {
            slbIds[i] = slbs.get(i).getId();
            dos[i] = SlbSlbStatusR.builder().slbId(slbs.get(i).getId()).onlineVersion(slbs.get(i).getVersion()).build();
        }
        Slb[] array = slbs.toArray(new Slb[slbs.size()]);
        slbSlbServerRMaintainer.refreshOnline(array);
        slbSlbStatusRMapper.batchUpdateSlbOnlineVersion(Arrays.asList(dos));
    }

    @Override
    public int delete(Long slbId) throws Exception {
        slbSlbServerRMaintainer.clear(slbId);
        slbSlbStatusRMapper.deleteByExample(new SlbSlbStatusRExample().createCriteria().andSlbIdEqualTo(slbId).example());
        int count = slbSlbMapper.deleteByPrimaryKey(slbId);
        slbArchiveSlbMapper.deleteByExample(new SlbArchiveSlbExample().createCriteria().andSlbIdEqualTo(slbId).example());
        return count;
    }

    private SlbSlb parseSlb(Long slbId, Slb slb) throws Exception {
        return slb == null ? null
                : SlbSlb.
                builder().
                id(slbId).
                name(slb.getName()).
                status(slb.getStatus()).
                version(slb.getVersion()).
                createdTime(new Date()).
                content(ObjectJsonWriter.write(slb)).build();
    }
}
