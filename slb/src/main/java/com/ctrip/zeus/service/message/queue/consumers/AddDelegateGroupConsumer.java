package com.ctrip.zeus.service.message.queue.consumers;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.executor.TaskManager;
import com.ctrip.zeus.lock.DbLockFactory;
import com.ctrip.zeus.lock.DistLock;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.queue.GroupData;
import com.ctrip.zeus.model.queue.Message;
import com.ctrip.zeus.model.queue.SlbMessageData;
import com.ctrip.zeus.model.queue.VsData;
import com.ctrip.zeus.model.task.OpsTask;
import com.ctrip.zeus.service.message.queue.AbstractConsumer;
import com.ctrip.zeus.service.message.queue.MessageQueue;
import com.ctrip.zeus.service.model.EntityFactory;
import com.ctrip.zeus.service.model.IdVersion;
import com.ctrip.zeus.service.model.ModelStatusMapping;
import com.ctrip.zeus.service.model.RuleRepository;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.ctrip.zeus.service.rule.model.RuleType;
import com.ctrip.zeus.service.task.constant.TaskOpsType;
import com.ctrip.zeus.tag.PropertyBox;
import com.ctrip.zeus.util.MessageUtil;
import com.ctrip.zeus.util.ObjectJsonWriter;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service("addDelegateGroupConsumer")
public class AddDelegateGroupConsumer extends AbstractConsumer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private RuleRepository ruleRepository;

    @Resource
    private DbLockFactory dbLockFactory;

    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;

    @Resource
    private MessageQueue messageQueue;

    @Resource
    private TaskManager taskManager;

    @Resource
    private EntityFactory entityFactory;

    @Resource
    private PropertyBox propertyBox;

    private final int timeout = 1000;

    private static DynamicLongProperty apiTimeout = DynamicPropertyFactory.getInstance().getLongProperty("api.timeout", 30000L);


    @Override
    public void onNewDelegateGroup(List<Message> messages) {
        for (Message msg : messages) {
            SlbMessageData messageData = MessageUtil.parserSlbMessageData(msg.getTargetData());

            if (messageData == null) return;

            List<Rule> namedGroupRules = addNamedGroupRule(messageData);
            List<Rule> normalGroupRules = addGroupsRule(messageData);
            List<Rule> vsRules = addVsRule(messageData);

            try {
                if (namedGroupRules.size() > 0) {
                    activateGroups(namedGroupRules);
                }
                if (normalGroupRules.size() > 0) {
                    activateGroups(normalGroupRules);
                }
                if (vsRules.size() > 0) {
                    activateVses(vsRules);
                }
            }catch (Exception e){
                logger.error("Failed to activate groups and VSes. Exception: " + e.getMessage());
            }

        }
    }

    /*Add rule on vs, so that rules of groups can inherit it*/
    private List<Rule> addVsRule(SlbMessageData messageData) {
        List<VsData> vsDatas = messageData.getVsDatas();
        List<Rule> addedRules = new ArrayList<>();

        if (vsDatas != null && vsDatas.size() > 0) {
            for (VsData vsData : vsDatas) {
                Long vsId = vsData.getId();
                try {
                    Rule addedRule = addVsRule(vsId);
                    if (addedRule == null) continue;
                    addedRules.add(addedRule);
                } catch (Exception e) {
                    logger.error("Failed to add sharding rule on VS: " + vsId + ", Error message:" + e.getMessage());
                }
            }
        }
        return addedRules;
    }

    /*Add shard rule for the Named group that is responsible for move traffic to other areas such as SHA*/
    private List<Rule> addNamedGroupRule(SlbMessageData messageData) {
        List<Rule> addedRules = new ArrayList<>();
        List<GroupData> groupDatas = messageData.getGroupDatas();
        if (groupDatas != null && groupDatas.size() > 0) {
            for (GroupData groupData : groupDatas) {
                Long groupId = groupData.getId();
                try {
                    Rule addedRule = addGroupRule(groupId, 0D, false);
                    if (addedRule != null) {
                        addedRules.add(addedRule);
                    }
                } catch (Exception e) {
                    logger.error("Failed to add sharding rule on Sharding Group: " + groupId + ", Error message:" + e.getMessage());
                }
            }
        }

        return addedRules;
    }

    /*Add shard rule for vs groups*/
    private List<Rule> addGroupsRule(SlbMessageData messageData) {
        boolean succeed = true;
        List<Rule> rulesCreated = new ArrayList<>();
        Long namedGroupId = 0L;

        List<GroupData> groupDatas = messageData.getGroupDatas();
        if (groupDatas != null && groupDatas.size() > 0) {
            for (GroupData groupData : groupDatas) {
                namedGroupId = groupData.getId();
            }
        }

        List<VsData> vsDatas = messageData.getVsDatas();
        if (vsDatas != null && vsDatas.size() > 0) {
            for (VsData vsData : vsDatas) {
                Long vsId = vsData.getId();
                try {
                    // get VS related groups except the naming group
                    Set<IdVersion> idVersions = groupCriteriaQuery.queryByVsId(vsId);
                    Long finalNamedGroupId = namedGroupId;
                    List<Long> targetIds = idVersions.stream().map(c -> c.getId()).distinct().filter(c -> !c.equals(finalNamedGroupId)).collect(Collectors.toList());
                    if (targetIds != null && targetIds.size() > 0) {
                        for (Long id : targetIds) {
                            try {
                                Rule added = addGroupRule(id, 0D,true);
                                if (added != null) {
                                    rulesCreated.add(added);
                                }
                            } catch (Exception ex) {
                                succeed = false;
                                logger.error("Failed to add sharding rule on Group: " + id + ", Error message:" + ex.getMessage());
                                break;
                            }
                        }
                    }

                } catch (Exception e) {
                    logger.error("Failed to get VS by vs id: " + vsId + ", exception: " + e.getMessage());
                    succeed = false;
                } finally {
                    /*Roll back*/
                    if (!succeed) {
                        // remove rules on groups
                        if (rulesCreated.size() > 0) {
                            try {
                                ruleRepository.removeRuleByIds(rulesCreated.stream().map(c -> c.getId()).collect(Collectors.toList()));
                            } catch (Exception e) {
                                logger.error("Failed to add clean rules on Groups, Error message:" + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return rulesCreated;
    }

    // Functions
    private Rule addVsRule(Long vsId) throws Exception {
        Rule createdRule = null;
        List<Rule> existingRules = Collections.EMPTY_LIST;

        List<Rule> rulesByTarget = ruleRepository.getRulesByTarget(vsId.toString(), RuleTargetType.VS.name());
        if (rulesByTarget != null) {
            existingRules = rulesByTarget.stream().filter(c -> c.getRuleType().equalsIgnoreCase(RuleType.SHARDING_RULE.name())).collect(Collectors.toList());
        }
        if (existingRules.size() == 0) {
            ShardingRuleAttribute shardingRuleAttribute = new ShardingRuleAttribute().setEnable(true).setPercent(1D);

            DistLock lock = null;
            String lockKey = getUpdateTargetLockKey(vsId.toString(), "_updateVs");
            if (lockKey != null) {
                lock = dbLockFactory.newLock(lockKey);
                lock.lock(timeout);
            }

            Rule targetRule = new Rule().
                    setRuleType(RuleType.SHARDING_RULE.name()).
                    setTargetId(vsId.toString()).
                    setTargetType(RuleTargetType.VS.name()).
                    setName("SHADINGRULE_FOR_VS_" + vsId).
                    setAttributes(ObjectJsonWriter.write(shardingRuleAttribute));

            List<Rule> rules = new ArrayList<>();
            rules.add(targetRule);
            try {
                List<Rule> results = ruleRepository.set(rules);
                createdRule = results.get(0);
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }

        return createdRule;
    }

    private void activateVses(List<Rule> rulesCreated) throws Exception {
        List<Long> vsIds = rulesCreated.stream().map(c -> Long.parseLong(c.getTargetId())).collect(Collectors.toList());
        for (Long vsId : vsIds) {
            activateVs(vsId);
        }
    }

    private Rule addGroupRule(Long groupId, Double percent, boolean enabled) throws Exception {
        Rule target = null;

        List<Rule> existingRules = Collections.EMPTY_LIST;

        List<Rule> rulesByTarget = ruleRepository.getRulesByTarget(groupId.toString(), RuleTargetType.GROUP.name());
        if (rulesByTarget != null) {
            existingRules = rulesByTarget.stream().filter(c -> c.getRuleType().equalsIgnoreCase(RuleType.SHARDING_RULE.name())).collect(Collectors.toList());
        }
        if (existingRules.size() == 0) {
            ShardingRuleAttribute shardingRuleAttribute = new ShardingRuleAttribute().setEnable(enabled).setPercent(percent);

            DistLock lock = null;
            String lockKey = getUpdateTargetLockKey(groupId.toString(), "_updateGroup");
            if (lockKey != null) {
                lock = dbLockFactory.newLock(lockKey);
                lock.lock(timeout);
            }

            Rule targetRule = new Rule().
                    setRuleType(RuleType.SHARDING_RULE.name()).
                    setTargetId(groupId.toString()).
                    setTargetType(RuleTargetType.GROUP.name()).
                    setName("SHADINGRULE_FOR_GROUP_" + groupId).
                    setAttributes(ObjectJsonWriter.write(shardingRuleAttribute));

            List<Rule> rules = new ArrayList<>();
            rules.add(targetRule);
            try {
                List<Rule> results = ruleRepository.set(rules);
                target = results.get(0);
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }

        return target;
    }

    private void activateGroups(List<Rule> rulesCreated) throws Exception {
        List<Long> groupIds = rulesCreated.stream().map(c -> Long.parseLong(c.getTargetId())).collect(Collectors.toList());

        for (Long groupId : groupIds) {
            activateGroup(groupId);
        }
    }

    private void activateGroup(Long groupId) throws Exception {
        if (groupId == null) return;

        Group offGroup = null;
        Group onGroup = null;

        ModelStatusMapping<VirtualServer> vsMapping = null;
        ModelStatusMapping<Group> mapping = entityFactory.getGroupsByIds(new Long[]{groupId});
        offGroup = mapping.getOfflineMapping().get(groupId);
        onGroup = mapping.getOnlineMapping().get(groupId);

        if (offGroup != null && onGroup != null) {
            // VS validation
            Set<Long> vsIds = new HashSet<>();
            for (GroupVirtualServer gvs : offGroup.getGroupVirtualServers()) {
                vsIds.add(gvs.getVirtualServer().getId());
            }
            vsMapping = entityFactory.getVsesByIds(vsIds.toArray(new Long[]{}));
            if (vsMapping.getOnlineMapping().size() == 0) {
                throw new ValidationException("Related vs is not activated.VsIds: " + vsIds);
            }

            List<OpsTask> tasks = new ArrayList<>();
            for (VirtualServer vs : vsMapping.getOnlineMapping().values()) {
                for (Long slbId : vs.getSlbIds()) {
                    OpsTask task = new OpsTask();
                    task.setCreateTime(new Date())
                            .setGroupId(groupId)
                            .setTargetSlbId(slbId)
                            .setOpsType(TaskOpsType.ACTIVATE_GROUP)
                            .setSkipValidate(true)
                            .setVersion(offGroup.getVersion());
                    tasks.add(task);
                }
            }

            String slbMessageData = MessageUtil.getMessageBuilder("slb", "/api/activate/group",
                    "rule set", true).bindGroups(new Group[]{offGroup}).build();
            messageQueue.produceMessage("/api/activate/group", offGroup.getId(), slbMessageData);

            List<Long> taskIds = taskManager.addTask(tasks);
            taskManager.getResult(taskIds, apiTimeout.get());
        }
    }

    public List<VirtualServer> activateVs(Long vsId) throws Exception {
        Long[] ids = new Long[]{vsId};

        ModelStatusMapping<VirtualServer> vsMap = entityFactory.getVsesByIds(ids);
        List<Long> taskIds = new ArrayList<>();
        List<VirtualServer> results = new ArrayList<>();

        VirtualServer offlineVersion = vsMap.getOfflineMapping().get(vsId);
        results.add(offlineVersion);

        Set<Long> offlineRelatedSlbIds = new HashSet<>();
        offlineRelatedSlbIds.addAll(offlineVersion.getSlbIds());

        Set<Long> onlineRelatedSlbIds = new HashSet<>();
        VirtualServer onlineVersion = vsMap.getOnlineMapping().get(vsId);
        if (onlineVersion != null) {
            onlineRelatedSlbIds.addAll(onlineVersion.getSlbIds());
        }
        Set<Long> tmp = new HashSet<>();
        tmp.addAll(offlineRelatedSlbIds);
        tmp.addAll(onlineRelatedSlbIds);
        ModelStatusMapping<Slb> slbMap = entityFactory.getSlbsByIds(tmp.toArray(new Long[tmp.size()]));
        List<OpsTask> tasks = new ArrayList<>();
        for (Long slbId : tmp) {
            Slb slb = slbMap.getOnlineMapping().get(slbId);
            if (slb == null) {
                if (offlineRelatedSlbIds.contains(slbId)) {
                    throw new ValidationException("Slb " + slbId + " is found deactivated.");
                } else {
                    throw new Exception("Slb " + slbId + " is found deactivated of an online vs. VsId=" + vsId);
                }
            }
            if (onlineRelatedSlbIds.contains(slbId) && !offlineRelatedSlbIds.contains(slbId)) {
                OpsTask task = new OpsTask();
                task.setSlbVirtualServerId(vsId)
                        .setTargetSlbId(slbId)
                        .setVersion(offlineVersion.getVersion())
                        .setOpsType(TaskOpsType.SOFT_DEACTIVATE_VS)
                        .setCreateTime(new Date());
                tasks.add(task);
            } else {
                OpsTask task = new OpsTask();
                task.setSlbVirtualServerId(vsId)
                        .setTargetSlbId(slbId)
                        .setVersion(offlineVersion.getVersion())
                        .setOpsType(TaskOpsType.ACTIVATE_VS)
                        .setCreateTime(new Date());
                tasks.add(task);
            }
        }
        taskIds.addAll(taskManager.addTask(tasks));

        try {
            propertyBox.set("status", "activated", "vs", ids);
        } catch (Exception ex) {
        }
        return results;
    }

    private String getUpdateTargetLockKey(String targetId, String lockKey) throws ValidationException {

        if (lockKey == null) {
            return null;
        }
        return targetId + lockKey;
    }

}
