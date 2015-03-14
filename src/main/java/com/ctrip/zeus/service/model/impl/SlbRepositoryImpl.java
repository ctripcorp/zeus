package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.SlbList;
import com.ctrip.zeus.service.model.ArchiveService;
import com.ctrip.zeus.service.model.SlbQuery;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.model.SlbSync;
import org.springframework.stereotype.Repository;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/5/2015.
 */
@Repository("slbClusterRepository")
public class SlbRepositoryImpl implements SlbRepository {

    @Resource
    private SlbSync slbSync;
    @Resource
    private SlbQuery slbQuery;

    @Resource
    private ArchiveService archiveService;

    @Override
    public SlbList list() {
        try {
            SlbList list = new SlbList();
            for (Slb slb : slbQuery.getAll()) {
                list.addSlb(slb);
            }
            list.setTotal(list.getSlbs().size());
            return list;
        } catch (DalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Slb get(String slbName) {
        try {
            return slbQuery.get(slbName);
        } catch (DalException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addOrUpdate(Slb s) {
        try {
            slbSync.sync(s);
            archiveService.archiveSlb(s);
        } catch (DalException e) {
            e.printStackTrace();
        }
    }
}
