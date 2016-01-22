package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.ArchiveService;

import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author:xingchaowang
 * @date: 3/14/2015.
 */
@Component("archiveService")
public class ArchiveServiceImpl implements ArchiveService {
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private ArchiveGroupDao archiveGroupDao;
    @Resource
    private ArchiveVsDao archiveVsDao;

    @Override
    public Archive getSlbArchive(Long slbId, int version) throws Exception {
        ArchiveSlbDo archive = archiveSlbDao.findBySlbAndVersion(slbId, version, ArchiveSlbEntity.READSET_FULL);
        return C.toSlbArchive(archive);
    }

    @Override
    public Archive getGroupArchive(Long groupId, int version) throws Exception {
        ArchiveGroupDo archive = archiveGroupDao.findByGroupAndVersion(groupId, version, ArchiveGroupEntity.READSET_FULL);
        return C.toGroupArchive(archive);
    }

    @Override
    public Archive getVsArchive(Long vsId, int version) throws Exception {
        MetaVsArchiveDo d = archiveVsDao.findByVsAndVersion(vsId, version, ArchiveVsEntity.READSET_FULL);
        return C.toVsArchive(d);
    }
}