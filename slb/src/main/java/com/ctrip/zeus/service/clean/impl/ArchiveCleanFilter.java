package com.ctrip.zeus.service.clean.impl;

import com.ctrip.zeus.dao.entity.SlbArchiveGroupExample;
import com.ctrip.zeus.dao.entity.SlbArchiveSlbExample;
import com.ctrip.zeus.dao.entity.SlbArchiveVsExample;
import com.ctrip.zeus.dao.mapper.SlbArchiveGroupMapper;
import com.ctrip.zeus.dao.mapper.SlbArchiveSlbMapper;
import com.ctrip.zeus.dao.mapper.SlbArchiveVsMapper;
import com.ctrip.zeus.service.clean.AbstractCleanFilter;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.SelectionMode;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.query.SlbCriteriaQuery;
import com.ctrip.zeus.service.query.VirtualServerCriteriaQuery;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by fanqq on 2015/10/21.
 */
@Service("archiveCleanFilter")
public class ArchiveCleanFilter extends AbstractCleanFilter {
    @Resource
    private SlbArchiveGroupMapper slbArchiveGroupMapper;
    @Resource
    private SlbArchiveSlbMapper slbArchiveSlbMapper;
    @Resource
    private SlbArchiveVsMapper slbArchiveVsMapper;
    @Resource
    private SlbCriteriaQuery slbCriteriaQuery;
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;
    @Resource
    private VirtualServerCriteriaQuery virtualServerCriteriaQuery;

    private static DynamicIntProperty archiveSaveCounts = DynamicPropertyFactory.getInstance().getIntProperty("archive.save.count", 100);

    @Override
    public void runFilter() throws Exception {
        int saveCount = archiveSaveCounts.get();
        // slb
        for (IdVersion key : slbCriteriaQuery.queryAll(SelectionMode.ONLINE_FIRST)) {
            slbArchiveSlbMapper.deleteByExample(new SlbArchiveSlbExample().createCriteria().andVersionLessThan(key.getVersion() - saveCount).andSlbIdEqualTo(key.getId()).example());
        }

        // group
        for (IdVersion key : groupCriteriaQuery.queryAll(SelectionMode.ONLINE_FIRST)) {
            slbArchiveGroupMapper.deleteByExample(new SlbArchiveGroupExample().createCriteria().andGroupIdEqualTo(key.getId()).andVersionLessThan(key.getVersion() - saveCount).example());
        }

        // vs
        for (IdVersion key : virtualServerCriteriaQuery.queryAll(SelectionMode.ONLINE_FIRST)) {
            slbArchiveVsMapper.deleteByExample(new SlbArchiveVsExample().createCriteria().andVsIdEqualTo(key.getId()).andVersionLessThan(key.getVersion()-saveCount).example());
        }
    }
}
