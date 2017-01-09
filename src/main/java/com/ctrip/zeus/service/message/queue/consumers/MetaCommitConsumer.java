package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.dal.core.*;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.queue.entity.*;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.model.Archive;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.util.MessageUtil;
import com.google.common.base.Joiner;
import org.springframework.stereotype.Service;
import org.unidal.dal.jdbc.DalException;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2016/12/15.
 */
@Service("metaCommitConsumer")
public class MetaCommitConsumer extends AbstractConsumer {
    @Resource
    private ArchiveGroupDao archiveGroupDao;
    @Resource
    private ArchiveVsDao archiveVsDao;
    @Resource
    private ArchiveSlbDao archiveSlbDao;
    @Resource
    private ArchiveCommitDao archiveCommitDao;

    @Override
    public void onUpdateGroup(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (GroupData gd : data.getGroupDatas()) {
                    try {
                        ArchiveGroupDo d0 = archiveGroupDao.findByGroupAndVersion(gd.getId(), gd.getVersion() - 1, ArchiveGroupEntity.READSET_FULL);
                        ArchiveGroupDo d = archiveGroupDao.findByGroupAndVersion(gd.getId(), gd.getVersion(), ArchiveGroupEntity.READSET_FULL);
                        List<String> description = diff(Group.class, ContentReaders.readGroupContent(d0.getContent()), ContentReaders.readGroupContent(d.getContent()));
                        archiveCommitDao.insert(new ArchiveCommitDo().setArchiveId(d.getId()).setType(MetaType.GROUP.getId()).setRefId(gd.getId()).setAuthor(m.getPerformer()).setMessage("Update " + Joiner.on(",").join(description)));
                    } catch (DalException e) {
                    } catch (SAXException e) {
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    @Override
    public void onNewGroup(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (GroupData gd : data.getGroupDatas()) {
                    try {
                        ArchiveGroupDo d = archiveGroupDao.findByGroupAndVersion(gd.getId(), gd.getVersion(), ArchiveGroupEntity.READSET_IDONLY);
                        archiveCommitDao.insert(new ArchiveCommitDo().setArchiveId(d.getId()).setType(MetaType.GROUP.getId()).setRefId(gd.getId()).setAuthor(m.getPerformer()).setMessage("Create"));
                    } catch (DalException e) {
                    }
                }
            }
        }
    }

    @Override
    public void onDeleteGroup(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (GroupData gd : data.getGroupDatas()) {
                    try {
                        archiveCommitDao.deleteByRefAndType(new ArchiveCommitDo().setRefId(gd.getId()).setType(MetaType.GROUP.getId()));
                    } catch (DalException e) {
                    }
                }
            }
        }
    }

    @Override
    public void onUpdateVs(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (VsData vd : data.getVsDatas()) {
                    try {
                        MetaVsArchiveDo d0 = archiveVsDao.findByVsAndVersion(vd.getId(), vd.getVersion() - 1, ArchiveVsEntity.READSET_FULL);
                        MetaVsArchiveDo d = archiveVsDao.findByVsAndVersion(vd.getId(), vd.getVersion(), ArchiveVsEntity.READSET_FULL);
                        List<String> description = diff(VirtualServer.class, ContentReaders.readVirtualServerContent(d0.getContent()), ContentReaders.readVirtualServerContent(d.getContent()));
                        archiveCommitDao.insert(new ArchiveCommitDo().setArchiveId(d.getId()).setType(MetaType.VS.getId()).setRefId(vd.getId()).setAuthor(m.getPerformer()).setMessage("Update " + Joiner.on(",").join(description)));
                    } catch (DalException e) {
                    } catch (SAXException e) {
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    @Override
    public void onNewVs(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (VsData vd : data.getVsDatas()) {
                    try {
                        MetaVsArchiveDo d = archiveVsDao.findByVsAndVersion(vd.getId(), vd.getVersion(), ArchiveVsEntity.READSET_IDONLY);
                        archiveCommitDao.insert(new ArchiveCommitDo().setArchiveId(d.getId()).setType(MetaType.VS.getId()).setRefId(vd.getId()).setAuthor(m.getPerformer()).setMessage("Create"));
                    } catch (DalException e) {
                    }
                }
            }
        }
    }

    @Override
    public void onDeleteVs(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (VsData vd : data.getVsDatas()) {
                    try {
                        archiveCommitDao.deleteByRefAndType(new ArchiveCommitDo().setRefId(vd.getId()).setType(MetaType.VS.getId()));
                    } catch (DalException e) {
                    }
                }
            }
        }
    }

    @Override
    public void onNewSlb(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (SlbData sd : data.getSlbDatas()) {
                    try {
                        ArchiveSlbDo d = archiveSlbDao.findBySlbAndVersion(sd.getId(), sd.getVersion(), ArchiveSlbEntity.READSET_IDONLY);
                        archiveCommitDao.insert(new ArchiveCommitDo().setArchiveId(d.getId()).setType(MetaType.SLB.getId()).setRefId(sd.getId()).setAuthor(m.getPerformer()).setMessage("Create"));
                    } catch (DalException e) {
                    }
                }
            }
        }
    }

    @Override
    public void onUpdateSlb(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (SlbData sd : data.getSlbDatas()) {
                    try {
                        ArchiveSlbDo d0 = archiveSlbDao.findBySlbAndVersion(sd.getId(), sd.getVersion() - 1, ArchiveSlbEntity.READSET_FULL);
                        ArchiveSlbDo d = archiveSlbDao.findBySlbAndVersion(sd.getId(), sd.getVersion(), ArchiveSlbEntity.READSET_FULL);
                        List<String> description = diff(Slb.class, ContentReaders.readSlbContent(d0.getContent()), ContentReaders.readSlbContent(d.getContent()));
                        archiveCommitDao.insert(new ArchiveCommitDo().setArchiveId(d.getId()).setType(MetaType.SLB.getId()).setRefId(sd.getId()).setAuthor(m.getPerformer()).setMessage("Update " + Joiner.on(",").join(description)));
                    } catch (DalException e) {
                    } catch (SAXException e) {
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    @Override
    public void onDeleteSlb(List<Message> messages) {
        for (Message m : messages) {
            SlbMessageData data = MessageUtil.parserSlbMessageData(m.getTargetData());
            if (data != null && data.getSuccess()) {
                for (SlbData sd : data.getSlbDatas()) {
                    try {
                        archiveCommitDao.deleteByRefAndType(new ArchiveCommitDo().setRefId(sd.getId()).setType(MetaType.SLB.getId()));
                    } catch (DalException e) {
                    }
                }
            }
        }
    }

    private static <T> List<String> diff(Class<T> metaType, T t1, T t2) {
        List<String> updatedProperties = new ArrayList<>();
        try {
            for (PropertyDescriptor property : Introspector.getBeanInfo(metaType).getPropertyDescriptors()) {
                if (property.getReadMethod() != null && !"class".equals(property.getName())
                        && !"version".equals(property.getName()) && !"createdTime".equals(property.getName())) {
                    try {
                        Object v1 = property.getReadMethod().invoke(t1);
                        Object v2 = property.getReadMethod().invoke(t2);
                        if ((v1 == null && v2 != null)
                                || (v1 != null && !v1.equals(v2))) {
                            updatedProperties.add(property.getName());
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (updatedProperties.size() == 0) {
                updatedProperties.add("pure version");
            }
            return updatedProperties;
        } catch (IntrospectionException e) {
        }
        return null;
    }
}
