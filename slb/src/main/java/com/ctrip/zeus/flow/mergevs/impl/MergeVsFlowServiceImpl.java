package com.ctrip.zeus.flow.mergevs.impl;

import com.ctrip.zeus.dao.entity.ToolsVsMerge;
import com.ctrip.zeus.dao.entity.ToolsVsMergeExample;
import com.ctrip.zeus.dao.mapper.ToolsVsMergeMapper;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.flow.mergevs.MergeVsFlowService;
import com.ctrip.zeus.flow.mergevs.model.MergeVsFlowEntity;
import com.ctrip.zeus.flow.mergevs.model.MergeVsFlowStepEntity;
import com.ctrip.zeus.flow.mergevs.model.MergeVsStatus;
import com.ctrip.zeus.flow.utils.TaskTools;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.ValidationFacade;
import com.ctrip.zeus.service.model.VirtualServerRepository;
import com.ctrip.zeus.service.nginx.CertificateService;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.util.AssertUtils;
import com.ctrip.zeus.util.MessageUtil;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service("mergeVsFlowService")
public class MergeVsFlowServiceImpl implements MergeVsFlowService {
    @Resource
    private ValidationFacade validationFacade;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Autowired
    private CertificateService certificateService;
    @Resource
    private TaskTools taskTools;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ToolsVsMergeMapper toolsVsMergeMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public MergeVsFlowEntity add(MergeVsFlowEntity entity) throws Exception {
        AssertUtils.assertNotNull(entity.getName(), "Name Can Not Be Null.");
        AssertUtils.assertNotNull(entity.getSourceVsId(), "Source Vs Id Can Not Be Null.");
        if (entity.getSourceVsId().size() == 0) {
            throw new ValidationException("Source Vses Can Not Be Empty.");
        }
        VirtualServer vs = virtualServerRepository.getById(entity.getSourceVsId().get(0));
        if (vs.getSsl()) {
            AssertUtils.assertNotNull(entity.getCid(), "CId Can Not Be Null.");
        }

        entity.setStatus(MergeVsStatus.CREATED);
        entity.setCreateAndBindNewVs(null);
        entity.setCreated(new MergeVsFlowStepEntity().setStartTime(new Date()).
                setFinishTime(new Date()).setStatus(MergeVsStatus.STEP_SUCCESS));
        entity.setCreateTime(new Date());
        entity.setNewVsId(null);
        entity.setMergeVs(null);

        ToolsVsMerge insert = ToolsVsMerge.builder().
                name(entity.getName()).
                content(ObjectJsonWriter.write(entity)).
                status(entity.getStatus()).
                build();

        toolsVsMergeMapper.insert(insert);
        return entity.setId(insert.getId());
    }

    @Override
    public MergeVsFlowEntity update(MergeVsFlowEntity entity, boolean force) throws Exception {
        AssertUtils.assertNotNull(entity.getId(), "Id Can Not Be Null.");
        AssertUtils.assertNotNull(entity.getName(), "Name Can Not Be Null.");
        AssertUtils.assertNotNull(entity.getSourceVsId(), "Source Vs Id Can Not Be Null.");
        if (entity.getSourceVsId().size() == 0) {
            throw new ValidationException("Source Vses Can Not Be Empty.");
        }

        MergeVsFlowEntity oldEntity = get(entity.getId());
        AssertUtils.assertNotNull(oldEntity, "Not Found Entity By Id:" + entity.getId());

        if (!force) {
            if (!MergeVsStatus.CREATED.equalsIgnoreCase(oldEntity.getStatus())
                    && !MergeVsStatus.FINISH_ROLLBACK.equalsIgnoreCase(oldEntity.getStatus())) {
                throw new ValidationException("Only Status \"Created\" Can Be Updated.");
            }
            entity.setStatus(MergeVsStatus.CREATED);
            entity.setCreateAndBindNewVs(null);
            entity.setCreated(new MergeVsFlowStepEntity().setStartTime(new Date()).
                    setFinishTime(new Date()).setStatus(MergeVsStatus.STEP_SUCCESS));
            entity.setCreateTime(new Date());
            entity.setNewVsId(null);
            entity.setMergeVs(null);

            ToolsVsMerge insert = ToolsVsMerge.builder().
                    name(entity.getName()).
                    content(ObjectJsonWriter.write(entity)).
                    status(entity.getStatus()).
                    id(entity.getId()).
                    build();

            toolsVsMergeMapper.updateByPrimaryKeySelective(insert);

        } else {
            ToolsVsMerge update = ToolsVsMerge.builder().
                    name(entity.getName()).
                    content(ObjectJsonWriter.write(entity)).
                    status(entity.getStatus()).
                    id(entity.getId()).
                    build();

            toolsVsMergeMapper.updateByPrimaryKeySelective(update);
        }
        return get(entity.getId());
    }

    @Override
    public MergeVsFlowEntity updateStep(Long id, String step) throws Exception {
        MergeVsFlowEntity entity = get(id);
        entity.setStatus(step);

        ToolsVsMerge update = ToolsVsMerge.builder().
                content(ObjectJsonWriter.write(entity)).
                status(entity.getStatus()).
                id(entity.getId()).
                build();

        toolsVsMergeMapper.updateByPrimaryKeySelective(update);
        return entity;
    }

    @Override
    public MergeVsFlowEntity get(Long id) throws Exception {
        ToolsVsMerge record = toolsVsMergeMapper.selectByPrimaryKey(id);
        if (record == null) return null;
        MergeVsFlowEntity entity = ObjectJsonParser.parse(new String(record.getContent()), MergeVsFlowEntity.class);
        if (entity == null) {
            return null;
        }
        entity.setId(record.getId());
        return entity;
    }

    @Override
    public List<MergeVsFlowEntity> queryAll() throws Exception {
        List<ToolsVsMerge> records = toolsVsMergeMapper.selectByExampleWithBLOBs(new ToolsVsMergeExample());
        List<MergeVsFlowEntity> result = new ArrayList<>();
        for (ToolsVsMerge record : records) {
            MergeVsFlowEntity entity = ObjectJsonParser.parse(new String(record.getContent()), MergeVsFlowEntity.class);
            if (entity == null) {
                return null;
            }
            entity.setId(record.getId());
            result.add(entity);
        }
        return result;
    }

    @Override
    public MergeVsFlowEntity createAndBindNewVs(Long id) throws Exception {
        MergeVsFlowEntity entity = get(id);
        if (entity == null) throw new ValidationException("Not Found Entity.ID:" + id);

        if (!MergeVsStatus.FAIL_BIND_NEW_VS.equalsIgnoreCase(entity.getStatus()) &&
                !MergeVsStatus.FAIL_MERGE_VS.equalsIgnoreCase(entity.getStatus()) &&
                !MergeVsStatus.FINISH_ROLLBACK.equalsIgnoreCase(entity.getStatus()) &&
                !MergeVsStatus.CREATED.equalsIgnoreCase(entity.getStatus())) {
            throw new ValidationException("Error Step Status.Require Step CREATED Or FAIL_BIND_NEW_VS.Get Step:" + entity.getStatus());
        }

        try {
            if (entity.getCreateAndBindNewVs() == null) {
                entity.setCreateAndBindNewVs(new MergeVsFlowStepEntity().setStartTime(new Date()).setStatus(MergeVsStatus.STEP_DOING));
            } else {
                entity.getCreateAndBindNewVs().setStatus(MergeVsStatus.STEP_DOING);
            }

            updateEntityAndStatus(entity, MergeVsStatus.START_BIND_NEW_VS);


            validationFacade.validateForMergeVs(entity.getSourceVsId(), entity.getId());

            List<VirtualServer> vses = virtualServerRepository.listAll(entity.getSourceVsId().toArray(new Long[]{}));
            if (vses.size() == 0) throw new ValidationException("VsIds Not Found.");
            List<String> domains = new ArrayList<>();
            for (VirtualServer vs : vses) {
                if (vs.getDomains() == null) continue;
                entity.addDomains(vs.getId(), vs.getDomains());
                for (Domain domain : vs.getDomains()) {
                    domains.add(domain.getName());
                }
            }
            updateEntityAndStatus(entity, MergeVsStatus.START_BIND_NEW_VS);

            if (vses.get(0).getSsl()) {
                if (entity.getCid() == null) {
                    throw new ValidationException("CID Cant Be Null In Case Of SSL VS Merge.");
                }
                certificateService.loadCertificate(Joiner.on("|").join(domains), entity.getCid(), 0L);
            }
            VirtualServer newVs = new VirtualServer();
            if (entity.getNewVsId() != null) {
                newVs = virtualServerRepository.getById(entity.getNewVsId());
            } else {
                VirtualServer tmp = vses.get(0);
                newVs.getSlbIds().addAll(tmp.getSlbIds());
                newVs.setSsl(tmp.getSsl());
                newVs.setPort(tmp.getPort());
                newVs.setName(tmp.getName() + "_mergeVs");
                newVs.getRuleSet().addAll(tmp.getRuleSet());

                //Create Vses
                newVs = virtualServerRepository.add(newVs);
                setProperty("status", "deactivated", "vs", new Long[]{newVs.getId()});
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/new",
                        "VS Merge Progress：" + entity.getId(), true).bindVses(new VirtualServer[]{newVs}).build();
                messageQueue.produceMessage("/api/vs/new", newVs.getId(), slbMessageData);
            }

            entity.setNewVsId(newVs.getId());

            taskTools.activateEntity(null, null, null, Collections.singletonList(newVs));

            taskTools.bindToNewVs(vses.get(0), Collections.singletonList(newVs.getId()));

            //TODO entity status
            entity.getCreateAndBindNewVs().setStatus(MergeVsStatus.STEP_SUCCESS);
            entity.getCreateAndBindNewVs().setMessage("");
            updateEntityAndStatus(entity, MergeVsStatus.FINISH_BIND_NEW_VS);
            return entity;
        } catch (Exception e) {
            entity.getCreateAndBindNewVs().setStatus(MergeVsStatus.STEP_FAIL);
            entity.getCreateAndBindNewVs().setMessage(e.getMessage());
            updateEntityAndStatus(entity, MergeVsStatus.FAIL_BIND_NEW_VS);
            logger.error("[[Flow=VsMerge]] Bind New Vs Failed. FlowId:" + entity, e);
            throw e;
        }
    }


    private void setProperty(String pname, String pvalue, String type, Long[] ids) {
        try {
            if (ids.length > 0) {
                propertyBox.set(pname, pvalue, type, ids);
            }
        } catch (Exception ex) {
            logger.error("[[Fail=SetProperty]] Set Property Failed.PName:" + pname + ";Pvalue:" + pvalue + ";type:" + type + ";Ids:" + ids);
        }
    }

    @Override
    public MergeVsFlowEntity mergeVs(Long id) throws Exception {
        MergeVsFlowEntity entity = get(id);
        if (entity == null) throw new ValidationException("Not Found Entity.ID:" + id);

        if (!MergeVsStatus.FINISH_BIND_NEW_VS.equalsIgnoreCase(entity.getStatus()) &&
                !MergeVsStatus.FAIL_MERGE_VS.equalsIgnoreCase(entity.getStatus())) {
            throw new ValidationException("Error Step Status.Require Step FAIL_MERGE_VS Or FINISH_BIND_NEW_VS Or FINISH_ROLLBACK.Get Step:" + entity.getStatus());
        }


        //Try lock
        List<DistLock> locks = new ArrayList<>();
        DistLock newVsLock = dbLockFactory.newLock(entity.getNewVsId() + "_updateVs");
        try {
            newVsLock.lock(3000);
            locks.add(newVsLock);
        } catch (Exception e) {
            newVsLock.unlock();
            logger.error("Lock VS Failed.", e);
            throw e;
        }
        for (Long vsId : entity.getSourceVsId()) {
            DistLock lock = dbLockFactory.newLock(vsId + "_updateVs");
            try {
                lock.lock(3000);
            } catch (Exception e) {
                for (DistLock l : locks) {
                    l.unlock();
                }
                logger.error("Lock VS Failed.", e);
                throw e;
            }
            locks.add(lock);
        }

        Map<Long, List<Domain>> orgBackUp = entity.getDomains();
        List<VirtualServer> orgVses = new ArrayList<>();
        VirtualServer newVs = null;

        try {
            if (entity.getMergeVs() == null) {
                entity.setMergeVs(new MergeVsFlowStepEntity().setStartTime(new Date()).setStatus(MergeVsStatus.STEP_DOING));
            } else {
                entity.getMergeVs().setStatus(MergeVsStatus.STEP_DOING);
            }

            updateEntityAndStatus(entity, MergeVsStatus.START_MERGE_VS);

            newVs = virtualServerRepository.getById(entity.getNewVsId());
            orgVses = virtualServerRepository.listAll(entity.getSourceVsId().toArray(new Long[]{}));
            List<VirtualServer> updatedVses = new ArrayList<>();
            for (VirtualServer t : orgVses) {
                if (t.getDomains() == null || t.getDomains().size() == 0) {
                    updatedVses.add(t);
                    continue;
                }
                t.getDomains().clear();
                updatedVses.add(virtualServerRepository.update(t));
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS Merge Progress：" + entity.getId(), true).bindVses(new VirtualServer[]{t}).build();
                messageQueue.produceMessage("/api/vs/update", t.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{t.getId()});
            }
            newVs.getDomains().clear();
            for (List<Domain> domains : orgBackUp.values()) {
                newVs.getDomains().addAll(domains);
            }
            updatedVses.add(virtualServerRepository.update(newVs));
            String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                    "VS Merge Progress：" + entity.getId(), true).bindVses(new VirtualServer[]{newVs}).build();
            messageQueue.produceMessage("/api/vs/update", newVs.getId(), slbMessageData);
            setProperty("status", "toBeActivated", "vs", new Long[]{newVs.getId()});
            taskTools.activateSyncVses(updatedVses);
            entity.getMergeVs().setStatus(MergeVsStatus.STEP_SUCCESS);
            entity.getMergeVs().setMessage("");
            updateEntityAndStatus(entity, MergeVsStatus.FINISH_MERGE_VS);
        } catch (Exception e) {
            entity.getMergeVs().setStatus(MergeVsStatus.STEP_FAIL);
            entity.getMergeVs().setMessage(e.getMessage());
            updateEntityAndStatus(entity, MergeVsStatus.FAIL_MERGE_VS);
            logger.error("[[Flow=VsMerge]] Merge Vs Failed. FlowId:" + entity, e);

            //Rollback
            if (newVs != null) {
                newVs.getDomains().clear();
                virtualServerRepository.update(newVs);
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS Merge Progress Revert：" + entity.getId(), true).bindVses(new VirtualServer[]{newVs}).build();
                messageQueue.produceMessage("/api/vs/update", newVs.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{newVs.getId()});
            }
            for (VirtualServer tmp : orgVses) {
                tmp.getDomains().clear();
                tmp.getDomains().addAll(orgBackUp.get(tmp.getId()));
                virtualServerRepository.update(tmp);
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS Merge Progress Revert：" + entity.getId(), true).bindVses(new VirtualServer[]{tmp}).build();
                messageQueue.produceMessage("/api/vs/update", tmp.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{tmp.getId()});
            }
            throw e;
        } finally {
            for (DistLock l : locks) {
                l.unlock();
            }
        }
        return entity;
    }

    @Override
    public MergeVsFlowEntity rollback(Long id) throws Exception {
        MergeVsFlowEntity entity = get(id);
        if (entity == null) throw new ValidationException("Not Found Entity.ID:" + id);

        if (!MergeVsStatus.FINISH_MERGE_VS.equalsIgnoreCase(entity.getStatus()) &&
                !MergeVsStatus.FAIL_ROLLBACK.equalsIgnoreCase(entity.getStatus())) {
            throw new ValidationException("Error Step Status.Require Step FINISH_MERGE_VS Or FAIL_ROLLBACK.Get Step:" + entity.getStatus());
        }


        //Try lock
        List<DistLock> locks = new ArrayList<>();
        DistLock newVsLock = dbLockFactory.newLock(entity.getNewVsId() + "_updateVs");
        try {
            newVsLock.lock(3000);
            locks.add(newVsLock);
        } catch (Exception e) {
            newVsLock.unlock();
            logger.error("Lock VS Failed.", e);
            throw e;
        }
        for (Long vsId : entity.getSourceVsId()) {
            DistLock lock = dbLockFactory.newLock(vsId + "_updateVs");
            try {
                lock.lock(3000);
            } catch (Exception e) {
                for (DistLock l : locks) {
                    l.unlock();
                }
                logger.error("Lock VS Failed.", e);
                throw e;
            }
            locks.add(lock);
        }

        Map<Long, List<Domain>> orgBackUp = entity.getDomains();
        List<VirtualServer> orgVses = new ArrayList<>();
        VirtualServer newVs = null;


        try {
            if (orgBackUp == null || orgBackUp.size() == 0) {
                throw new ValidationException("Domains Error.Entity Id:" + entity.getId());
            }
            if (entity.getRollback() == null) {
                entity.setRollback(new MergeVsFlowStepEntity().setStartTime(new Date()).setStatus(MergeVsStatus.STEP_DOING));
            } else {
                entity.getRollback().setStatus(MergeVsStatus.STEP_DOING);
            }

            updateEntityAndStatus(entity, MergeVsStatus.START_ROLLBACK);

            newVs = virtualServerRepository.getById(entity.getNewVsId());
            orgVses = virtualServerRepository.listAll(entity.getSourceVsId().toArray(new Long[]{}));
            List<VirtualServer> vses = new ArrayList<>();

            if (newVs != null) {
                newVs.getDomains().clear();
                vses.add(virtualServerRepository.update(newVs));
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS Merge Progress Revert：" + entity.getId(), true).bindVses(new VirtualServer[]{newVs}).build();
                messageQueue.produceMessage("/api/vs/update", newVs.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{newVs.getId()});
            }
            for (VirtualServer tmp : orgVses) {
                tmp.getDomains().clear();
                tmp.getDomains().addAll(orgBackUp.get(tmp.getId()));
                virtualServerRepository.update(tmp);
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS Merge Progress Revert：" + entity.getId(), true).bindVses(new VirtualServer[]{tmp}).build();
                messageQueue.produceMessage("/api/vs/update", tmp.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{tmp.getId()});
            }
            vses.addAll(orgVses);

            taskTools.activateSyncVses(vses);

            taskTools.unbindCleanVses(Collections.singletonList(entity.getNewVsId()));
            entity.setCreateAndBindNewVs(null);
            entity.setMergeVs(null);
            entity.setNewVsId(null);
            entity.getDomains().clear();

            entity.getRollback().setStatus(MergeVsStatus.STEP_SUCCESS);
            entity.getRollback().setMessage("");
            updateEntityAndStatus(entity, MergeVsStatus.FINISH_ROLLBACK);

        } catch (Exception e) {
            entity.getRollback().setStatus(MergeVsStatus.STEP_FAIL);
            entity.getRollback().setMessage(e.getMessage());
            updateEntityAndStatus(entity, MergeVsStatus.FAIL_ROLLBACK);
            logger.error("[[Flow=VsMerge]] Rollback Failed. FlowId:" + entity, e);
        } finally {
            for (DistLock l : locks) {
                l.unlock();
            }
        }
        return entity;
    }

    @Override
    public MergeVsFlowEntity disable(Long id) throws Exception {
        return updateStep(id, MergeVsStatus.DISABLED);
    }

    @Override
    public MergeVsFlowEntity delete(Long id) throws Exception {
        MergeVsFlowEntity entity = get(id);
        if (entity == null) {
            throw new NotFoundException("Not Found Entity By Id:" + id);
        } else {
            toolsVsMergeMapper.deleteByPrimaryKey(id);
        }
        return entity;
    }

    @Override
    public MergeVsFlowEntity clean(Long id) throws Exception {
        MergeVsFlowEntity entity = get(id);
        if (entity == null) throw new ValidationException("Not Found Entity.ID:" + id);

        if (!MergeVsStatus.FINISH_MERGE_VS.equalsIgnoreCase(entity.getStatus()) &&
                !MergeVsStatus.FAIL_CLEAN.equalsIgnoreCase(entity.getStatus())) {
            throw new ValidationException("Error Step Status.Require Step FINISH_MERGE_VS Or FAIL_CLEAN.Get Step:" + entity.getStatus());
        }
        try {
            if (entity.getCleanVs() == null) {
                entity.setCleanVs(new MergeVsFlowStepEntity().setStartTime(new Date()).setStatus(MergeVsStatus.STEP_DOING));
            } else {
                entity.getCleanVs().setStatus(MergeVsStatus.STEP_DOING);
            }

            updateEntityAndStatus(entity, MergeVsStatus.START_CLEAN);

            taskTools.unbindCleanVses(entity.getSourceVsId());
            entity.getCleanVs().setStatus(MergeVsStatus.STEP_SUCCESS);
            entity.getCleanVs().setMessage("");
            updateEntityAndStatus(entity, MergeVsStatus.FINISH_CLEAN);
            return entity;
        } catch (Exception e) {
            entity.getCleanVs().setStatus(MergeVsStatus.STEP_FAIL);
            entity.getCleanVs().setMessage(e.getMessage());
            updateEntityAndStatus(entity, MergeVsStatus.FAIL_CLEAN);
            logger.error("[[Flow=VsMerge]] Merge Vs Failed. FlowId:" + entity, e);
            throw e;
        }

    }

    private void updateEntityAndStatus(MergeVsFlowEntity entity, String status) throws Exception {
        entity.setStatus(status);
        ToolsVsMerge update = ToolsVsMerge.builder().
                content(ObjectJsonWriter.write(entity)).
                status(entity.getStatus()).
                id(entity.getId()).
                build();

        toolsVsMergeMapper.updateByPrimaryKeySelective(update);
    }
}


