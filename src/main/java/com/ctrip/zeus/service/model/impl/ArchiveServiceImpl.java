package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.service.model.ArchiveService;

import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.ContentWriters;
import com.ctrip.zeus.support.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Resource
    private ConfGroupActiveDao confGroupActiveDao;
    @Resource
    private ConfSlbActiveDao confSlbActiveDao;
    @Resource
    private ConfSlbVirtualServerActiveDao confSlbVirtualServerActiveDao;

    @Override
    public Slb getSlb(Long slbId, int version) throws Exception {
        ArchiveSlbDo d = archiveSlbDao.findBySlbAndVersion(slbId, version, ArchiveSlbEntity.READSET_FULL);
        return d == null ? null : ContentReaders.readSlbContent(d.getContent());
    }

    @Override
    public Group getGroup(Long groupId, int version) throws Exception {
        ArchiveGroupDo d = archiveGroupDao.findByGroupAndVersion(groupId, version, ArchiveGroupEntity.READSET_FULL);
        return d == null ? null : ContentReaders.readGroupContent(d.getContent());
    }

    @Override
    public VirtualServer getVirtualServer(Long vsId, int version) throws Exception {
        MetaVsArchiveDo d = archiveVsDao.findByVsAndVersion(vsId, version, ArchiveVsEntity.READSET_FULL);
        return d == null ? null : ContentReaders.readVirtualServerContent(d.getContent());
    }

    @Override
    public Slb getLatestSlb(Long slbId) throws Exception {
        ArchiveSlbDo d = archiveSlbDao.findMaxVersionBySlb(slbId, ArchiveSlbEntity.READSET_FULL);
        return d == null ? null : ContentReaders.readSlbContent(d.getContent());
    }

    @Override
    public Group getLatestGroup(Long groupId) throws Exception {
        ArchiveGroupDo d = archiveGroupDao.findMaxVersionByGroup(groupId, ArchiveGroupEntity.READSET_FULL);
        return d == null ? null : ContentReaders.readGroupContent(d.getContent());
    }

    @Override
    public List<Slb> getLatestSlbs(Long[] slbIds) throws Exception {
        List<Slb> slbs = new ArrayList<>();
        for (ArchiveSlbDo archiveSlbDo : archiveSlbDao.findMaxVersionBySlbs(slbIds, ArchiveSlbEntity.READSET_FULL)) {
            try {
                Slb slb = ContentReaders.readSlbContent(archiveSlbDo.getContent());
                slbs.add(slb);
            } catch (Exception ex) {
                slbs.add(new Slb().setId(archiveSlbDo.getId()));
            }
        }
        return slbs;
    }

    @Override
    public List<Group> getLatestGroups(Long[] groupIds) throws Exception {
        List<Group> groups = new ArrayList<>();
        for (ArchiveGroupDo archiveGroupDo : archiveGroupDao.findMaxVersionByGroups(groupIds, ArchiveGroupEntity.READSET_FULL)) {
            try {
                Group group = ContentReaders.readGroupContent(archiveGroupDo.getContent());
                groups.add(group);
            } catch (Exception ex) {
                groups.add(new Group().setId(archiveGroupDo.getId()));
            }
        }
        return groups;
    }

    @Override
    public Archive getLatestSlbArchive(Long slbId) throws Exception {
        ArchiveSlbDo asd = archiveSlbDao.findMaxVersionBySlb(slbId, ArchiveSlbEntity.READSET_FULL);
        return C.toSlbArchive(asd);
    }

    @Override
    public List<Archive> getLastestVsArchives(Long[] vsIds) throws Exception {
        List<Archive> result = new ArrayList<>();
        for (MetaVsArchiveDo metaVsArchiveDo : archiveVsDao.findMaxVersionByVses(vsIds, ArchiveVsEntity.READSET_FULL)) {
            result.add(C.toVsArchive(metaVsArchiveDo));
        }
        return result;
    }

    @Override
    public Archive getLatestVsArchive(Long vsId) throws Exception {
        MetaVsArchiveDo d = archiveVsDao.findMaxVersionByVs(vsId, ArchiveVsEntity.READSET_FULL);
        return C.toVsArchive(d);
    }

    @Override
    public List<Group> getGroupsByIdAndVersion(Long[] groupIds, Integer[] versions) throws Exception {
        String[] pairs = new String[groupIds.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = groupIds[i] + "," + versions[i];
        }
        List<ArchiveGroupDo> list = archiveGroupDao.findAllByGroupAndVersion(groupIds, pairs, ArchiveGroupEntity.READSET_IDONLY);
        Long[] ids = new Long[list.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = list.get(i).getId();
        }
        list = archiveGroupDao.findAllByIds(ids, ArchiveGroupEntity.READSET_FULL);
        List<Group> result = new ArrayList<>(list.size());
        for (ArchiveGroupDo d : list) {
            result.add(ContentReaders.readGroupContent(d.getContent()));
        }
        return result;
    }

    @Override
    public List<VirtualServer> getVirtualServersByIdAndVersion(Long[] vsIds, Integer[] versions) throws Exception {
        String[] pairs = new String[vsIds.length];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = vsIds[i] + "," + versions[i];
        }
        List<MetaVsArchiveDo> list = archiveVsDao.findAllByVsAndVersion(vsIds, pairs, ArchiveVsEntity.READSET_IDONLY);
        Long[] ids = new Long[list.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = list.get(i).getId();
        }
        list = archiveVsDao.findAllByIds(ids, ArchiveVsEntity.READSET_FULL);
        List<VirtualServer> result = new ArrayList<>(list.size());
        for (MetaVsArchiveDo d : list) {
            result.add(ContentReaders.readVirtualServerContent(d.getContent()));
        }
        return result;
    }

    @Override
    public String upgradeArchives(Long[] slbIds, Long[] groupIds, Long[] vsIds) throws Exception {
        String err = "Upgrade Slb failed on id: ";

        Map<String, ArchiveSlbDo> slbs = new HashMap<>();

        for (ArchiveSlbDo d : archiveSlbDao.findMaxVersionBySlbs(slbIds, ArchiveSlbEntity.READSET_FULL)) {
            try {
                Slb slb = DefaultSaxParser.parseEntity(Slb.class, d.getContent());
                d.setContent(ContentWriters.writeSlbContent(slb));
                slbs.put(d.getId() + "," + d.getVersion(), d);
            } catch (Exception ex) {
                err += d.getSlbId() + ",";
            }
        }
        for (ConfSlbActiveDo d : confSlbActiveDao.findAll(ConfSlbActiveEntity.READSET_FULL)) {
            if (!slbs.containsKey(d.getSlbId() + "," + d.getVersion())) {
                try {
                    ArchiveSlbDo e = archiveSlbDao.findBySlbAndVersion(d.getSlbId(), d.getVersion(), ArchiveSlbEntity.READSET_FULL);
                    Slb slb = DefaultSaxParser.parseEntity(Slb.class, e.getContent());
                    e.setContent(ContentWriters.writeSlbContent(slb));
                    slbs.put(e.getId() + "," + e.getVersion(), e);
                } catch (Exception ex) {
                    err += d.getSlbId() + ",";
                }
            }
        }
        archiveSlbDao.updateContentById(slbs.values().toArray(new ArchiveSlbDo[slbs.size()]), ArchiveSlbEntity.UPDATESET_FULL);

        err += " upgrade group failed on id: ";

        Map<String, ArchiveGroupDo> groups = new HashMap<>();

        for (ArchiveGroupDo d : archiveGroupDao.findMaxVersionByGroups(groupIds, ArchiveGroupEntity.READSET_FULL)) {
            try {
                Group group = DefaultSaxParser.parseEntity(Group.class, d.getContent());
                d.setContent(ContentWriters.writeGroupContent(group));
                groups.put(d.getId() + "," + d.getVersion(), d);
            } catch (Exception ex) {
                err += d.getGroupId() + ",";
            }
        }
        for (ConfGroupActiveDo d : confGroupActiveDao.findAll(ConfGroupActiveEntity.READSET_FULL)) {
            if (!groups.containsKey(d.getGroupId() + "," + d.getVersion())) {
                try {
                    ArchiveGroupDo e = archiveGroupDao.findByGroupAndVersion(d.getGroupId(), d.getVersion(), ArchiveGroupEntity.READSET_FULL);
                    Group group = DefaultSaxParser.parseEntity(Group.class, e.getContent());
                    e.setContent(ContentWriters.writeGroupContent(group));
                    groups.put(e.getId() + "," + e.getVersion(), e);
                } catch (Exception ex) {
                    err += d.getGroupId() + ",";
                }
            }
        }
        archiveGroupDao.updateContentById(groups.values().toArray(new ArchiveGroupDo[groups.size()]), ArchiveGroupEntity.UPDATESET_FULL);

        err += " upgrade virtual server failed on id: ";

        Map<String, MetaVsArchiveDo> vses = new HashMap<>();

        for (MetaVsArchiveDo d : archiveVsDao.findMaxVersionByVses(vsIds, ArchiveVsEntity.READSET_FULL)) {
            try {
                VirtualServer vs = DefaultSaxParser.parseEntity(VirtualServer.class, d.getContent());
                d.setContent(ContentWriters.writeVirtualServerContent(vs));
                vses.put(d.getId() + "," + d.getVersion(), d);
            } catch (Exception ex) {
                err += d.getVsId() + ",";
            }
        }
        for (ConfSlbVirtualServerActiveDo d : confSlbVirtualServerActiveDao.findBySlbVirtualServerIds(vsIds, ConfSlbVirtualServerActiveEntity.READSET_FULL)) {
            if (vses.containsKey(d.getSlbVirtualServerId() + "," + d.getVersion())) {
                try {
                    MetaVsArchiveDo e = archiveVsDao.findByVsAndVersion(d.getSlbVirtualServerId(), d.getVersion(), ArchiveVsEntity.READSET_FULL);
                    VirtualServer vs = DefaultSaxParser.parseEntity(VirtualServer.class, d.getContent());
                    d.setContent(ContentWriters.writeVirtualServerContent(vs));
                    vses.put(d.getId() + "," + d.getVersion(), e);
                } catch (Exception ex) {
                    err += d.getSlbVirtualServerId() + ",";
                }
            }
        }
        archiveVsDao.updateContentById(vses.values().toArray(new MetaVsArchiveDo[vses.size()]), ArchiveVsEntity.UPDATESET_FULL);
        return err;
    }
}