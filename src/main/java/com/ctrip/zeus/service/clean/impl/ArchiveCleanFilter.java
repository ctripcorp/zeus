package com.ctrip.zeus.service.clean.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.service.clean.AbstractCleanFilter;
import com.ctrip.zeus.service.clean.CleanFilter;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * Created by fanqq on 2015/10/21.
 */
@Service("archiveCleanFilter")
public class ArchiveCleanFilter extends AbstractCleanFilter{
    @Resource
    private ArchiveGroupDao archiveGroupDao ;
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;

    private static DynamicIntProperty archiveSaveCounts = DynamicPropertyFactory.getInstance().getIntProperty("archive.save.count", 100);

    @Override
    public void runFilter() throws Exception {
        Set<Long> slbIds = slbCriteriaQuery.queryAll();
        List<ArchiveSlbDo> slbs = archiveSlbDao.findMaxVersionBySlbs(slbIds.toArray(new Long[]{}), ArchiveSlbEntity.READSET_FULL);
        for (ArchiveSlbDo archiveSlbDo : slbs){
            archiveSlbDao.deleteBySlbIdLessThanVersion(new ArchiveSlbDo().setSlbId(archiveSlbDo.getSlbId()).setVersion(archiveSlbDo.getVersion() - archiveSaveCounts.get()));
        }

        Set<Long> groupIds = groupCriteriaQuery.queryAll();
        List<ArchiveGroupDo> groups = archiveGroupDao.findAllByGroups(groupIds.toArray(new Long[]{}),ArchiveGroupEntity.READSET_FULL);
        for (ArchiveGroupDo archiveGroupDo : groups){
            archiveGroupDao.deleteByGroupIdLessThanVersion(new ArchiveGroupDo().setGroupId(archiveGroupDo.getGroupId()).setVersion(archiveGroupDo.getVersion() - archiveSaveCounts.get()));
        }
    }
}
