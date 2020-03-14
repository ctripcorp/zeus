package com.ctrip.zeus.flow.splitvs.impl;

import com.ctrip.zeus.dao.entity.ToolsVsSplit;
import com.ctrip.zeus.dao.entity.ToolsVsSplitExample;
import com.ctrip.zeus.dao.mapper.ToolsVsSplitMapper;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.flow.splitvs.SplitVsFlowService;
import com.ctrip.zeus.flow.splitvs.model.SplitVsFlowEntity;
import com.ctrip.zeus.flow.splitvs.model.SplitVsFlowStepEntity;
import com.ctrip.zeus.flow.splitvs.model.SplitVsStatus;
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


@Service("splitVsFlowService")
public class SplitVsFlowServiceImpl implements SplitVsFlowService {

    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private DbLockFactory dbLockFactory;
    @Resource
    private ValidationFacade validationFacade;
    @Autowired
    private CertificateService certificateService;
    @Resource
    private PropertyBox propertyBox;
    @Resource
    private TaskTools taskTools;
    @Resource
    private MessageQueue messageQueue;
    @Resource
    private ToolsVsSplitMapper toolsVsSplitMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public SplitVsFlowEntity add(SplitVsFlowEntity entity) throws Exception {
        AssertUtils.assertNotNull(entity.getName(), "Name Can Not Be Null.");
        AssertUtils.assertNotNull(entity.getSourceVsId(), "Source Vs Id Can Not Be Null.");

        entity.setStatus(SplitVsStatus.CREATED);
        entity.setCreateAndBindNewVs(null);
        entity.setCreated(new SplitVsFlowStepEntity().setStartTime(new Date()).
                setFinishTime(new Date()).setStatus(SplitVsStatus.STEP_SUCCESS));
        entity.setCreateTime(new Date());
        entity.setNewVsIds(new ArrayList<Long>());
        entity.setSplitVs(null);

        ToolsVsSplit record = ToolsVsSplit.builder()
                .name(entity.getName())
                .content(ObjectJsonWriter.write(entity))
                .status(entity.getStatus()).build();

        toolsVsSplitMapper.insert(record);
        return entity.setId(record.getId());
    }

    @Override
    public SplitVsFlowEntity update(SplitVsFlowEntity entity, boolean force) throws Exception {
        AssertUtils.assertNotNull(entity.getId(), "Id Can Not Be Null.");
        AssertUtils.assertNotNull(entity.getName(), "Name Can Not Be Null.");
        AssertUtils.assertNotNull(entity.getSourceVsId(), "Source Vs Id Can Not Be Null.");
        AssertUtils.assertNotNull(entity.getDomainGroups(), "Domain Groups Can Not Be Null.");
        if (entity.getDomainGroups().size() == 0) {
            throw new ValidationException("Domain Groups Can Not Be Empty.");
        }

        SplitVsFlowEntity oldEntity = get(entity.getId());
        AssertUtils.assertNotNull(oldEntity, "Not Found Entity By Id:" + entity.getId());

        if (!force) {
            if (!SplitVsStatus.CREATED.equalsIgnoreCase(oldEntity.getStatus()) &&
                    !SplitVsStatus.FINISH_ROLLBACK.equalsIgnoreCase(oldEntity.getStatus())) {
                throw new ValidationException("Only Status \"Created\" Can Be Updated.");
            }
            entity.setStatus(SplitVsStatus.CREATED);
            entity.setCreateAndBindNewVs(null);
            entity.setCreated(new SplitVsFlowStepEntity().setStartTime(new Date()).
                    setFinishTime(new Date()).setStatus(SplitVsStatus.STEP_SUCCESS));
            entity.setCreateTime(new Date());
            entity.setNewVsIds(new ArrayList<Long>());
            entity.setSplitVs(null);

            ToolsVsSplit record = new ToolsVsSplit();
            record.setName(entity.getName());
            record.setContent(ObjectJsonWriter.write(entity));
            record.setStatus(entity.getStatus());
            record.setId(entity.getId());
            toolsVsSplitMapper.updateByPrimaryKeySelective(record);
        } else {
            ToolsVsSplit record = new ToolsVsSplit();
            record.setName(entity.getName());
            record.setContent(ObjectJsonWriter.write(entity));
            record.setStatus(entity.getStatus());
            record.setId(entity.getId());

            toolsVsSplitMapper.updateByPrimaryKeySelective(record);
        }

        return get(entity.getId());
    }

    @Override
    public SplitVsFlowEntity updateStep(Long id, String step) throws Exception {
        SplitVsFlowEntity entity = get(id);
        if (entity == null) throw new ValidationException("Not Found Entity.ID:" + id);
        entity.setStatus(step);
        ToolsVsSplit record = new ToolsVsSplit();
        record.setContent(ObjectJsonWriter.write(entity));
        record.setStatus(entity.getStatus());
        record.setId(entity.getId());
        toolsVsSplitMapper.updateByPrimaryKeySelective(record);
        return entity;
    }

    @Override
    public SplitVsFlowEntity get(Long id) throws Exception {
        ToolsVsSplit record = toolsVsSplitMapper.selectByPrimaryKey(id);
        if (record == null) return null;
        SplitVsFlowEntity entity = ObjectJsonParser.parse(new String(record.getContent()), SplitVsFlowEntity.class);
        if (entity == null) {
            return null;
        }
        entity.setId(record.getId());
        return entity;
    }

    @Override
    public List<SplitVsFlowEntity> queryAll() throws Exception {
        List<ToolsVsSplit> flowVsSplitDoes = toolsVsSplitMapper.selectByExampleWithBLOBs(new ToolsVsSplitExample());
        List<SplitVsFlowEntity> result = new ArrayList<>();
        for (ToolsVsSplit record : flowVsSplitDoes) {
            SplitVsFlowEntity entity = ObjectJsonParser.parse(new String(record.getContent()), SplitVsFlowEntity.class);
            if (entity == null) {
                continue;
            }
            entity.setId(record.getId());
            result.add(entity);
        }
        return result;
    }

    @Override
    public SplitVsFlowEntity createAndBindNewVs(Long id) throws Exception {
        SplitVsFlowEntity entity = get(id);
        if (entity == null) throw new ValidationException("Not Found Entity.ID:" + id);
        AssertUtils.assertNotNull(entity.getDomainGroups(), "Domain Groups Can Not Be Null.");
        if (entity.getDomainGroups().size() == 0) {
            throw new ValidationException("Domain Groups Can Not Be Empty.");
        }

        if (!SplitVsStatus.FAIL_BIND_NEW_VS.equalsIgnoreCase(entity.getStatus()) &&
                !SplitVsStatus.FAIL_SPLIT_VS.equalsIgnoreCase(entity.getStatus()) &&
                !SplitVsStatus.FINISH_ROLLBACK.equalsIgnoreCase(entity.getStatus()) &&
                !SplitVsStatus.CREATED.equalsIgnoreCase(entity.getStatus())) {
            throw new ValidationException("Error Step Status.Require Step CREATED Or FAIL_BIND_NEW_VS.Get Step:" + entity.getStatus());
        }

        try {
            if (entity.getCreateAndBindNewVs() == null) {
                entity.setCreateAndBindNewVs(new SplitVsFlowStepEntity().setStartTime(new Date()).setStatus(SplitVsStatus.STEP_DOING));
            } else {
                entity.getCreateAndBindNewVs().setStatus(SplitVsStatus.STEP_DOING);
            }

            updateEntityAndStatus(entity, SplitVsStatus.START_BIND_NEW_VS);

            validationFacade.validateForSplitVs(entity.getSourceVsId(), entity.getId());

            VirtualServer vs = virtualServerRepository.getById(entity.getSourceVsId());
            List<String> domains = new ArrayList<>();
            for (Domain domain : vs.getDomains()) {
                domains.add(domain.getName());
            }
            List<List<String>> domainGroups = entity.getDomainGroups();
            List<VirtualServer> newVses = new ArrayList<>();
            Set<String> allNewDomains = new HashSet<>();
            int allNewListSize = 0;
            for (List<String> tmp : domainGroups) {
                allNewDomains.addAll(tmp);
                allNewListSize += tmp.size();
            }

            if (allNewDomains.size() != allNewListSize) {
                throw new ValidationException("Domain List Has Duplicate Domain Value.");
            }
            if (allNewDomains.size() != domains.size() || !allNewDomains.containsAll(domains)) {
                throw new ValidationException("Domain List Don't Match Domains On Vs.");
            }

            boolean isFirst = true;
            for (List<String> tmp : domainGroups) {
                if (vs.getSsl()) {
                    String domainList = Joiner.on("|").join(tmp);
                    String cid = certificateService.getActivatedCId(entity.getSourceVsId());
                    if (cid != null) {
                        certificateService.loadCertificate(domainList, cid, 0L);
                    } else {
                        throw new ValidationException("Certificate Of Origin Vs Not Have " +
                                "An Cid. Please Re-Install Certificate For Origin Vs:VsId" + entity.getSourceVsId());
                    }
                    certificateService.getCertificateOnBoard(domainList);
                }

                if (tmp.size() == 0) continue;
                if (isFirst) {
                    isFirst = false;
                    continue;
                }

                VirtualServer tmpVs = new VirtualServer();
                tmpVs.setName(tmp.get(0) + "_" + vs.getPort() + "_" + entity.getName());
                tmpVs.setPort(vs.getPort());
                tmpVs.setSsl(vs.getSsl());
                tmpVs.getSlbIds().addAll(vs.getSlbIds());
                tmpVs.getRuleSet().addAll(vs.getRuleSet());
                newVses.add(tmpVs);
            }

            //Create Vses
            if (entity.getNewVsIds().size() == 0) {
                entity.getNewVsIds().add(entity.getSourceVsId());
            }
            List<VirtualServer> newVsesList = new ArrayList<>();
            for (int i = 0; i < newVses.size(); i++) {
                if (i >= entity.getNewVsIds().size() - 1) {
                    VirtualServer v = newVses.get(i);
                    v = virtualServerRepository.add(v);
                    newVsesList.add(v);
                    String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/new",
                            "VS拆分流程：" + entity.getId(), true).bindVses(new VirtualServer[]{v}).build();
                    messageQueue.produceMessage("/api/vs/new", v.getId(), slbMessageData);
                    setProperty("status", "deactivated", "vs", new Long[]{v.getId()});
                    entity.getNewVsIds().add(v.getId());
                }
            }

            taskTools.activateEntity(null, null, null, newVsesList);
            taskTools.bindToNewVs(vs, entity.getNewVsIds());
            //TODO entity status
            entity.getCreateAndBindNewVs().setStatus(SplitVsStatus.STEP_SUCCESS);
            entity.getCreateAndBindNewVs().setMessage("");
            updateEntityAndStatus(entity, SplitVsStatus.FINISH_BIND_NEW_VS);
            return entity;
        } catch (Exception e) {
            entity.getCreateAndBindNewVs().setStatus(SplitVsStatus.STEP_FAIL);
            entity.getCreateAndBindNewVs().setMessage(e.getMessage());
            updateEntityAndStatus(entity, SplitVsStatus.FAIL_BIND_NEW_VS);
            logger.error("[[Flow=VsSplit]] Bind New Vs Failed. FlowId:" + entity, e);
            throw e;
        }
    }

    private void updateEntityAndStatus(SplitVsFlowEntity entity, String status) throws Exception {
        ToolsVsSplit record = new ToolsVsSplit();
        entity.setStatus(status);
        record.setContent(ObjectJsonWriter.write(entity));
        record.setStatus(entity.getStatus());
        record.setId(entity.getId());
        toolsVsSplitMapper.updateByPrimaryKeySelective(record);
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


    /**
     * Lock All Related Vses. And Update Domains, Then Try Activate All Vses in agg task.
     * Rollback in case of failed. Just update vses to base version. Do not activate them.
     * Exception Failed Lock / update / activate
     * */
    @Override
    public SplitVsFlowEntity splitVs(Long id) throws Exception {
        SplitVsFlowEntity entity = get(id);
        if (entity == null) throw new ValidationException("Not Found Entity.ID:" + id);

        if (!SplitVsStatus.FAIL_SPLIT_VS.equalsIgnoreCase(entity.getStatus()) &&
                !SplitVsStatus.FINISH_BIND_NEW_VS.equalsIgnoreCase(entity.getStatus())) {
            throw new ValidationException("Error Step Status.Require Step FAIL_SPLIT_VS Or FINISH_BIND_NEW_VS.Get Step:" + entity.getStatus());
        }

        //Try lock
        List<DistLock> locks = new ArrayList<>();
        DistLock orgVsLock = dbLockFactory.newLock(entity.getSourceVsId() + "_updateVs");
        try {
            orgVsLock.lock(3000);
            locks.add(orgVsLock);
        } catch (Exception e) {
            orgVsLock.unlock();
            logger.error("Lock VS Failed.", e);
            throw e;
        }
        for (Long vsId : entity.getNewVsIds()) {
            if (vsId.equals(entity.getSourceVsId())) {
                continue;
            }
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
        List<Domain> orgBackUp = new ArrayList<>();
        List<VirtualServer> vses = null;
        try {

            if (entity.getSplitVs() == null) {
                entity.setSplitVs(new SplitVsFlowStepEntity().setStartTime(new Date()).setStatus(SplitVsStatus.STEP_DOING));
            } else {
                entity.getSplitVs().setStatus(SplitVsStatus.STEP_DOING);
            }

            updateEntityAndStatus(entity, SplitVsStatus.START_SPLIT_VS);
            List<Long> ids = entity.getNewVsIds();
            vses = virtualServerRepository.listAll(ids.toArray(new Long[]{}));
            List<VirtualServer> updatedVses = new ArrayList<>();
            Map<Long, VirtualServer> vsMap = new HashMap<>();
            for (VirtualServer t : vses) {
                vsMap.put(t.getId(), t);
            }
            List<List<String>> domainGroups = entity.getDomainGroups();
            for (int i = 0; i < domainGroups.size() && i < ids.size(); i++) {
                List<String> tmp = domainGroups.get(i);
                VirtualServer tmpVs = vsMap.get(ids.get(i));
                if (tmpVs != null) {
                    tmpVs.getDomains().clear();
                    for (String domain : tmp) {
                        tmpVs.addDomain(new Domain().setName(domain));
                        orgBackUp.add(new Domain().setName(domain));
                    }
                    updatedVses.add(tmpVs);
                }
            }

            for (VirtualServer tmp : updatedVses) {
                virtualServerRepository.update(tmp);
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS拆分流程：" + entity.getId(), true).bindVses(new VirtualServer[]{tmp}).build();
                messageQueue.produceMessage("/api/vs/update", tmp.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{tmp.getId()});
            }
            taskTools.activateSyncVses(updatedVses);
            entity.getSplitVs().setStatus(SplitVsStatus.STEP_SUCCESS);
            entity.getSplitVs().setMessage("");
            updateEntityAndStatus(entity, SplitVsStatus.FINISH_SPLIT_VS);
            return entity;
        } catch (Exception e) {
            entity.getSplitVs().setStatus(SplitVsStatus.STEP_FAIL);
            entity.getSplitVs().setMessage(e.getMessage().substring(0, 512));
            updateEntityAndStatus(entity, SplitVsStatus.FAIL_SPLIT_VS);
            //Rollback
            if (vses!=null){
                for (VirtualServer tmp : vses) {
                    tmp.getDomains().clear();
                    virtualServerRepository.update(tmp);
                    String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                            "VS拆分流程回退：" + entity.getId(), true).bindVses(new VirtualServer[]{tmp}).build();
                    messageQueue.produceMessage("/api/vs/update", tmp.getId(), slbMessageData);
                    setProperty("status", "toBeActivated", "vs", new Long[]{tmp.getId()});
                }
            }
            VirtualServer vs = virtualServerRepository.getById(entity.getSourceVsId());
            if (vs != null) {
                vs.getDomains().clear();
                vs.getDomains().addAll(orgBackUp);
                virtualServerRepository.update(vs);
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS拆分流程回退：" + entity.getId(), true).bindVses(new VirtualServer[]{vs}).build();
                messageQueue.produceMessage("/api/vs/update", vs.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{vs.getId()});
            }
            logger.error("[[Flow=VsSplit]] Bind New Vs Failed. FlowId:" + entity, e);
            throw e;
        } finally {
            for (DistLock l : locks) {
                l.unlock();
            }
        }

    }

    @Override
    public SplitVsFlowEntity rollback(Long id) throws Exception {
        SplitVsFlowEntity entity = get(id);
        if (entity == null) throw new ValidationException("Not Found Entity.ID:" + id);

        if (!SplitVsStatus.FAIL_ROLLBACK.equalsIgnoreCase(entity.getStatus()) &&
                !SplitVsStatus.FINISH_SPLIT_VS.equalsIgnoreCase(entity.getStatus())) {
            throw new ValidationException("Error Step Status.Require Step FAIL_ROLLBACK Or FINISH_BIND_NEW_VS.Get Step:" + entity.getStatus());
        }

        //Try lock
        List<DistLock> locks = new ArrayList<>();
        DistLock orgVsLock = dbLockFactory.newLock(entity.getSourceVsId() + "_updateVs");
        try {
            orgVsLock.lock(3000);
            locks.add(orgVsLock);
        } catch (Exception e) {
            orgVsLock.unlock();
            logger.error("Lock VS Failed.", e);
            throw e;
        }
        for (Long vsId : entity.getNewVsIds()) {
            if (vsId.equals(entity.getSourceVsId())) {
                continue;
            }
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
        List<Domain> orgBackUp = new ArrayList<>();
        List<VirtualServer> vses = null;
        try {

            if (entity.getRollback() == null) {
                entity.setRollback(new SplitVsFlowStepEntity().setStartTime(new Date()).setStatus(SplitVsStatus.STEP_DOING));
            } else {
                entity.getRollback().setStatus(SplitVsStatus.STEP_DOING);
            }

            updateEntityAndStatus(entity, SplitVsStatus.START_ROLLBACK);
            List<Long> ids = entity.getNewVsIds();
            vses = virtualServerRepository.listAll(ids.toArray(new Long[]{}));
            List<VirtualServer> updatedVses = new ArrayList<>();

            List<List<String>> domainGroups = entity.getDomainGroups();
            for (List<String> tmp : domainGroups) {
                if (tmp == null) continue;
                for (String domain : tmp) {
                    orgBackUp.add(new Domain().setName(domain));
                }
            }

            for (VirtualServer tmp : vses) {
                tmp.getDomains().clear();
                updatedVses.add(virtualServerRepository.update(tmp));
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS拆分流程回退：" + entity.getId(), true).bindVses(new VirtualServer[]{tmp}).build();
                messageQueue.produceMessage("/api/vs/update", tmp.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{tmp.getId()});
            }
            VirtualServer vs = virtualServerRepository.getById(entity.getSourceVsId());
            if (vs != null) {
                vs.getDomains().clear();
                vs.getDomains().addAll(orgBackUp);
                updatedVses.add(virtualServerRepository.update(vs));
                String slbMessageData = MessageUtil.getMessageBuilder("SlbFlow", "/api/vs/update",
                        "VS拆分流程回退：" + entity.getId(), true).bindVses(new VirtualServer[]{vs}).build();
                messageQueue.produceMessage("/api/vs/update", vs.getId(), slbMessageData);
                setProperty("status", "toBeActivated", "vs", new Long[]{vs.getId()});
            }
            taskTools.activateSyncVses(updatedVses);

            //Clean Data
            ids.remove(entity.getSourceVsId());
            taskTools.unbindCleanVses(ids);
            entity.setCreateAndBindNewVs(null);
            entity.setSplitVs(null);
            entity.getNewVsIds().clear();

            entity.getRollback().setStatus(SplitVsStatus.STEP_SUCCESS);
            entity.getRollback().setMessage("");
            updateEntityAndStatus(entity, SplitVsStatus.FINISH_ROLLBACK);
            return entity;
        } catch (Exception e) {
            entity.getRollback().setStatus(SplitVsStatus.STEP_FAIL);
            entity.getRollback().setMessage(e.getMessage().substring(0, 512));
            updateEntityAndStatus(entity, SplitVsStatus.FAIL_ROLLBACK);
            throw e;
        } finally {
            for (DistLock l : locks) {
                l.unlock();
            }
        }
    }

    @Override
    public SplitVsFlowEntity disable(Long id) throws Exception {
        return updateStep(id, SplitVsStatus.DISABLED);
    }

    @Override
    public SplitVsFlowEntity delete(Long id) throws Exception {
        SplitVsFlowEntity entity = get(id);
        if (entity == null) {
            throw new NotFoundException("Not Found Entity By Id:" + id);
        } else {
            toolsVsSplitMapper.deleteByPrimaryKey(id);
        }
        return entity;
    }
}
