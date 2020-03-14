package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.dao.entity.RuleRuleExample;
import com.ctrip.zeus.dao.entity.RuleRuleTargetR;
import com.ctrip.zeus.dao.entity.RuleRuleTargetRExample;
import com.ctrip.zeus.dao.entity.RuleRuleWithBLOBs;
import com.ctrip.zeus.dao.mapper.RuleRuleMapper;
import com.ctrip.zeus.dao.mapper.RuleRuleTargetRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.model.*;
import com.ctrip.zeus.service.model.common.RuleTargetType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.handler.impl.ContentReaders;
import com.ctrip.zeus.service.model.handler.impl.ContentWriters;
import com.ctrip.zeus.service.model.validation.RuleValidator;
import com.ctrip.zeus.service.rule.model.RuleTargetDefaultValues;
import com.ctrip.zeus.service.rule.model.RuleType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2017/1/9.
 */
@Repository("ruleRepository")
public class RuleRepositoryImpl implements RuleRepository {

    @Resource
    private ValidationFacade validationFacade;

    @Resource
    private RuleValidator ruleValidator;

    @Autowired
    private GroupRepository groupRepository;

    @Resource
    private VirtualServerRepository virtualServerRepository;

    @Resource
    private TrafficPolicyRepository trafficPolicyRepository;
    @Resource
    private SlbRepository slbRepository;

    @Resource
    private RuleRuleMapper ruleRuleMapper;

    @Resource
    private RuleRuleTargetRMapper ruleRuleTargetRMapper;

    @Resource
    private ConfigHandler configHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public Rule add(Rule rule) throws Exception {
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        // save
        List<Rule> results = save(rules, true);
        // update target
        updateTarget(results, false);
        return results.get(0);
    }

    @Override
    public List<Rule> add(List<Rule> rules) throws Exception {
        // validate
        ruleValidator.checkRulesType(rules);
        // save
        List<Rule> results = save(rules, true);
        // update
        updateTarget(results, false);

        return results;
    }

    @Override
    public Rule update(Rule rule) throws Exception {
        List<Rule> rules = new ArrayList<>();
        rules.add(rule);
        // validate
        List<Rule> results = this.save(rules, false);

        // update target
        updateTarget(results, false);
        return results.get(0);
    }

    @Override
    public List<Rule> update(List<Rule> rules) throws Exception {
        // validate
        ruleValidator.checkRulesType(rules);
        // save
        List<Rule> results = this.save(rules, false);
        // update target
        updateTarget(results, false);
        return results;
    }

    @Override
    public List<Rule> set(List<Rule> rules) throws Exception {
        ruleValidator.checkRulesType(rules);
        List<Rule> tobeAdded;
        List<Rule> results = new ArrayList<>();

        for (Rule rule : rules) {
            tobeAdded = new ArrayList<>();
            tobeAdded.add(rule);
            if (rule.getId() == null) {
                results.addAll(this.save(tobeAdded, true));
            } else {
                results.addAll(this.save(tobeAdded, false));
            }
        }

        // update target
        updateTarget(results, false);
        return results;
    }

    @Override
    public List<Rule> list(List<Long> ids) throws Exception {
        return getRulesByRuleIds(ids);
    }

    @Override
    public List<Rule> getRulesByTarget(String targetId, String targetType) throws Exception {
        if (targetId == null || targetType == null)
            throw new ValidationException("Target type and target id is required");

        List<String> targetIds = new ArrayList<>();
        targetIds.add(targetId);
        Map<String, List<Rule>> rules = getRulesByTargets(targetIds, targetType);

        List<Rule> results = new ArrayList<>();
        List<Rule> found = rules.get(targetId);
        if (found != null && found.size() > 0) {
            results = found;
        }

        return results;
    }

    @Override
    public List<Rule> getDefaultRules() throws Exception {
        // Default rules
        List<Rule> defaultRules = getRulesByTarget(RuleTargetDefaultValues.DEFAULT_TARGET_ID, RuleTargetType.DEFAULT.name());
        if (!configHandler.getEnable("use.system.rule", false)) {
            return defaultRules;
        }

        List<Rule> result = new ArrayList<>();

        Map<String, List<Rule>> defaultRulesMap = new HashMap<>();
        for (Rule rule : defaultRules) {
            if (defaultRulesMap.get(rule.getRuleType()) == null) {
                defaultRulesMap.put(rule.getRuleType(), new ArrayList<>());
            }
            defaultRulesMap.get(rule.getRuleType()).add(rule);
        }

        // System rules
        Map<String, String[]> ctsMap = getSystemRules();

        // Merge
        Set<String> keys = new HashSet<>(ctsMap.keySet());
        keys.addAll(defaultRulesMap.keySet());
        for (String key : keys) {
            if (defaultRulesMap.containsKey(key)) {
                result.addAll(defaultRulesMap.get(key));
            } else {
                addSystemRule(result, key, ctsMap.get(key));
            }
        }

        return result;
    }

    @Override
    public List<Rule> removeRuleByIds(List<Long> ids) throws Exception {
        return removeRulesByRuleIds(ids);
    }

    @Override
    public List<Rule> removeRulesByTarget(RuleTargetType target, String targetId) throws Exception {
        if (target == null || targetId == null) throw new ValidationException("Target type and target id is required");

        List<String> targetIds = new ArrayList<>();
        targetIds.add(targetId);

        List<RuleRuleTargetR> targetRS = ruleRuleTargetRMapper.selectByExample(new RuleRuleTargetRExample().
                createCriteria().
                andTargetIdIn(targetIds).
                andTargetTypeEqualTo(target.getName()).
                example());

        if (targetRS.size() == 0) {
            logger.info("Could not find any rule with on target:" + target.getName() + ", target id=" + targetId);
            return new ArrayList<>();
        }


        List<Long> ids = new ArrayList<>();
        for (RuleRuleTargetR targetR : targetRS) {
            ids.add(targetR.getRuleId());
        }
        List<Rule> result = removeRulesByRuleIds(ids);

        return result;
    }

    private List<Rule> getRulesByRuleIds(List<Long> ids) throws Exception {
        List<Rule> results = new ArrayList<>();
        if (ids == null || ids.size() == 0) {
            return results;
        }
        List<RuleRuleWithBLOBs> rules = ruleRuleMapper.selectByExampleWithBLOBs(new RuleRuleExample().createCriteria().andIdIn(ids).example());
        for (RuleRuleWithBLOBs rule : rules) {
            results.add(ContentReaders.readRuleContent(rule.getContent()).setCreatedTime(rule.getDatachangeLasttime()).setId(rule.getId()));
        }
        return results;
    }

    public void setValidationFacade(ValidationFacade validationFacade) {
        this.validationFacade = validationFacade;
    }

    public void setRuleValidator(RuleValidator ruleValidator) {
        this.ruleValidator = ruleValidator;
    }

    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void setVirtualServerRepository(VirtualServerRepository virtualServerRepository) {
        this.virtualServerRepository = virtualServerRepository;
    }

    public void setSlbRepository(SlbRepository slbRepository) {
        this.slbRepository = slbRepository;
    }


    // Key: targetId
    // Value: rules related to target id
    private Map<String, List<Rule>> getRulesByTargets(List<String> targetIds, String targetType) throws Exception {
        Map<String, List<Rule>> results = new HashMap<>();

        List<Long> ruleIds = new ArrayList<>();
        List<RuleRuleTargetR> ruleRuleTargetRS = ruleRuleTargetRMapper.selectByExample(new RuleRuleTargetRExample().
                createCriteria().
                andTargetIdIn(targetIds).
                andTargetTypeEqualTo(targetType).
                example());

        if (ruleRuleTargetRS == null || ruleRuleTargetRS.size() == 0) {
            return results;
        }
        for (RuleRuleTargetR ruleRuleTargetR : ruleRuleTargetRS) {
            ruleIds.add(ruleRuleTargetR.getRuleId());
        }

        // get all rules by ids
        List<Rule> rules = getRulesByRuleIds(ruleIds);
        for (Rule rule : rules) {
            String targetId = rule.getTargetId();
            if (!results.containsKey(targetId)) {
                results.put(targetId, new ArrayList<Rule>());
            }
            results.get(targetId).add(rule);
        }

        return results;
    }

    private List<Rule> removeRulesByRuleIds(List<Long> ids) throws Exception {
        List<Rule> results = new ArrayList<>();

        // Validate
        if (ids == null || ids.size() == 0) throw new ValidationException("Rules to be removed shall not be blank");
        List<RuleRuleWithBLOBs> rules = ruleRuleMapper.selectByExampleWithBLOBs(new RuleRuleExample().
                createCriteria().
                andIdIn(ids).
                example());
        if (rules == null || rules.size() != ids.size()) {
            throw new ValidationException("Not all ids existed: " + StringUtils.join(ids, ','));
        }

        ruleRuleMapper.deleteByExample(new RuleRuleExample().createCriteria().andIdIn(ids).example());
        ruleRuleTargetRMapper.deleteByExample(new RuleRuleTargetRExample().createCriteria().andRuleIdIn(ids).example());

        for (RuleRuleWithBLOBs rule : rules) {
            results.add(ContentReaders.readRuleContent(rule.getContent()).setCreatedTime(rule.getDatachangeLasttime()).setId(rule.getId()));
        }

        // Update target
        updateTarget(results, true);
        return results;
    }

    private List<Rule> save(List<Rule> rules, boolean booleanAdding) throws Exception {
        // validate
        for (Rule rule : rules) {
            validateRule(rule);
            if (booleanAdding) {
                Long ruleEntityId = rule.getId();
                if (ruleEntityId == null || ruleEntityId != 0L) {
                    rule.setId(0L);
                }
                ruleValidator.checkRuleNew(rule);
                return addRules(rules);
            } else {
                ruleValidator.checkRuleUpdate(rule);
                return updateRules(rules);
            }
        }

        return null;
    }

    private void validateRule(Rule rule) throws Exception {
        // Rule entity
        if (rule == null) throw new ValidationException("Saved rule shall not be empty");
        ValidationContext context = new ValidationContext();
        validationFacade.validateRule(rule, context);
        if (context.getErrorRules().contains(rule.getId())) {
            throw new ValidationException(context.getRuleErrorReason(rule.getId()));
        }
    }

    private List<Rule> addRules(List<Rule> rules) throws Exception {
        List<Rule> results = new ArrayList<>();

        for (Rule rule : rules) {
            rule.setId(0L);
            RuleRuleWithBLOBs row = parseMybatisRule(rule);
            ruleRuleMapper.insert(row);
            rule.setId(row.getId());
            ruleRuleTargetRMapper.insert(parseMybatisRuleRel(rule.getId(), rule.getTargetId(), rule.getTargetType()));
            results.add(ContentReaders.readRuleContent(row.getContent()).setId(row.getId()).setCreatedTime(row.getDatachangeLasttime()));
        }
        return results;
    }

    private List<Rule> updateRules(List<Rule> rules) throws Exception {
        List<Rule> results = new ArrayList<>();

        for (Rule rule : rules) {
            RuleRuleWithBLOBs row = parseMybatisRule(rule);
            int updatedRow = ruleRuleMapper.updateByExampleWithBLOBs(row,
                    new RuleRuleExample().
                            createCriteria().
                            andIdEqualTo(row.getId()).
                            example());
            if (updatedRow == 0) {
                throw new ValidationException("Update rule failed, 0 rows effected");
            }
            results.add(ContentReaders.readRuleContent(row.getContent()).setId(row.getId()).setCreatedTime(row.getDatachangeLasttime()));
        }
        return results;
    }

    private RuleRuleWithBLOBs parseMybatisRule(Rule rule) {
        return RuleRuleWithBLOBs.builder().
                name(rule.getName())
                .ruleType(RuleType.getRuleType(rule.getRuleType()).getId())
                .attributes(rule.getAttributes())
                .content(ContentWriters.writeRuleContent(rule))
                .id(rule.getId() > 0 ? rule.getId() : 0L)
                .build();
    }

    private RuleRuleTargetR parseMybatisRuleRel(Long ruleId, String targetId, String targetType) {
        return RuleRuleTargetR.builder()
                .ruleId(ruleId)
                .targetId(targetId)
                .targetType(targetType)
                .build();
    }

    private void updateTarget(List<Rule> rules, boolean isRemovingRule) throws Exception {
        if (rules == null || rules.size() == 0) throw new ValidationException("Rules of target shall not be empty");

        String targetType = "";
        String targetId = "";

        // validate rule is for single target
        String key = null;
        for (Rule rule : rules) {
            targetType = rule.getTargetType();
            targetId = rule.getTargetId();
            String temp = targetType + "_" + targetId;
            if (key == null) {
                key = temp;
            } else if (!key.equalsIgnoreCase(temp)) {
                throw new ValidationException("Rules is not for same target");
            }
        }
        RuleTargetType target = RuleTargetType.getTargetType(targetType);

        if (!target.isNeedTarget()) return;

        switch (target) {
            case GROUP: {
                Long groupId = RuleTargetType.parseLongTargetId(targetId);
                updateTargetGroup(groupId, rules, isRemovingRule);
                break;
            }
            case VS: {
                Long vsId = RuleTargetType.parseLongTargetId(targetId);
                updateTargetVirtualServer(vsId, rules, isRemovingRule);
                break;
            }
            case SLB: {
                Long slbId = RuleTargetType.parseLongTargetId(targetId);
                updateTargetSlb(slbId, rules, isRemovingRule);
                break;
            }
            case TRAFFIC_POLICY: {
                Long policyId = RuleTargetType.parseLongTargetId(targetId);
                updateTargetTrafficPolicy(policyId, rules, isRemovingRule);
                break;
            }
            default: {
                throw new ValidationException("[Rule][Update Target] Target type " + targetType + ", is not supported");
            }
        }
    }

    private void updateTargetTrafficPolicy(Long trafficPolicyId, List<Rule> rules, boolean isRemovingRule) throws Exception {
        if (trafficPolicyId == null) return;

        TrafficPolicy t = trafficPolicyRepository.getById(trafficPolicyId);
        if (t == null || rules == null) {
            logger.warn("[Rule][Traffic Policy Update]: Traffic Policy entity or rules entity is empty");
            return;
        }

        // update traffic policy's rule set
        updateTargetRuleSet(t.getRuleSet(), rules, isRemovingRule);
        trafficPolicyRepository.update(t);
    }

    private void updateTargetGroup(Long groupId, List<Rule> rules, boolean isRemovingRule) throws Exception {

        if (groupId == null) return;

        Group g = groupRepository.getById(groupId);
        if (g == null || rules == null) {
            logger.warn("[Rule][Group Update]: Group entity or rule entity is empty");
            return;
        }

        updateTargetRuleSet(g.getRuleSet(), rules, isRemovingRule);
        groupRepository.updateGroupRules(g);
    }

    private void updateTargetVirtualServer(Long vsId, List<Rule> rules, boolean isRemovingRule) throws Exception {
        if (vsId == null) return;

        VirtualServer v = virtualServerRepository.getById(vsId);
        if (v == null || rules == null) {
            logger.warn("[Rule][VS Update]: VS entity or rule entity is empty");
            return;
        }

        updateTargetRuleSet(v.getRuleSet(), rules, isRemovingRule);
        virtualServerRepository.updateVirtualServerRule(v);
    }

    private void updateTargetSlb(Long slbId, List<Rule> rules, boolean isRemovingRule) throws Exception {
        if (slbId == null) return;

        Slb s = slbRepository.getById(slbId);
        if (s == null || rules == null) {
            logger.warn("[Rule][SLB Update]: SLB entity or rule entity is empty");
            return;
        }

        updateTargetRuleSet(s.getRuleSet(), rules, isRemovingRule);
        slbRepository.updateSlbRules(s);
    }

    private void updateTargetRuleSet(List<Rule> targetRules, List<Rule> updatedRules, boolean isRemovingRule) {
        List<Long> ids = new ArrayList<>();
        for (Rule r : targetRules) {
            ids.add(r.getId());
        }

        List<Long> toBeRemovedRules = new ArrayList<>();
        for (Rule rule : updatedRules) {
            Long ruleId = rule.getId();
            int index = ids.indexOf(ruleId);
            if (index != -1) {
                toBeRemovedRules.add(rule.getId());
            }
        }
        if (toBeRemovedRules.size() > 0) {
            Iterator<Rule> it = targetRules.iterator();
            while (it.hasNext()) {
                if (toBeRemovedRules.indexOf(it.next().getId()) != -1) {
                    it.remove();
                }
            }
        }

        if (!isRemovingRule) {
            targetRules.addAll(updatedRules);
        }
    }

    private void addSystemRule(List<Rule> systemRules, String ruleType, String[] ruleAttributes) {
        if (ruleAttributes == null || ruleAttributes.length == 0) return;

        for (String ruleAttribute : ruleAttributes) {
            if (!StringUtils.isEmpty(ruleAttribute.trim())) {
                systemRules.add(new Rule().setRuleType(ruleType).
                        setTargetType(RuleTargetType.DEFAULT.name()).
                        setTargetId(RuleTargetDefaultValues.DEFAULT_TARGET_ID).
                        setName(ruleType).setAttributes(ruleAttribute));
            }
        }
    }

    private Map<String, String[]> getSystemRules() {
        Map<String, String[]> result = new HashMap<>();
        result.put(RuleType.CLIENT_MAX_BODY_SIZE.name(), getSystemDefaultSettings(RuleType.CLIENT_MAX_BODY_SIZE, "{\"client-max-body-size\":30}"));

        result.put(RuleType.PROXY_READ_TIMEOUT.name(), getSystemDefaultSettings(RuleType.PROXY_READ_TIMEOUT, "{\"proxy-read-timeout\":120}"));

        result.put(RuleType.UPSTREAM_KEEP_ALIVE_TIMEOUT.name(), getSystemDefaultSettings(RuleType.UPSTREAM_KEEP_ALIVE_TIMEOUT, "{\"upstream-keep-alive-timeout\":50}"));

        result.put(RuleType.SET_REQUEST_HEADER.name(), getSystemDefaultSettings(RuleType.SET_REQUEST_HEADER, ""));

        result.put(RuleType.SOCKET_IO_ENABLED.name(), getSystemDefaultSettings(RuleType.SOCKET_IO_ENABLED, ""));

        result.put(RuleType.GROUP_ERROR_PAGE_ENABLE.name(), getSystemDefaultSettings(RuleType.GROUP_ERROR_PAGE_ENABLE, ""));

        result.put(RuleType.FAVICON_RULE.name(), getSystemDefaultSettings(RuleType.FAVICON_RULE, ""));

        result.put(RuleType.UPSTREAM_KEEP_ALIVE_COUNT.name(), getSystemDefaultSettings(RuleType.UPSTREAM_KEEP_ALIVE_COUNT, "{\"upstream-keep-alive-count\":50}"));

        result.put(RuleType.DEFAULT_SSL_CONFIG.name(), getSystemDefaultSettings(RuleType.DEFAULT_SSL_CONFIG, "{\"ssl-prefer-server-ciphers\":true,\"ssl-ecdh-curve\":\"X25519:P-256:P-384:P-224:P-521\",\"ssl-ciphers\":\"ECDHE+AES128:RSA+AES128:ECDHE+AES256:RSA+AES256:ECDHE+3DES:RSA+3DES\",\"ssl-buffer-size\":6,\"ssl-protocol\":\"TLSv1 TLSv1.1 TLSv1.2 TLSv1.3\"}"));

        result.put(RuleType.ERROR_PAGE.name(), getSystemDefaultSettings(RuleType.ERROR_PAGE, "{\"enabled\":false}"));

        result.put(RuleType.SSL_CONFIG.name(), getSystemDefaultSettings(RuleType.SSL_CONFIG, "{\"ssl-prefer-server-ciphers\":true,\"ssl-ciphers\":\"EECDH+CHACHA20:EECDH+CHACHA20-draft:EECDH+AES128:RSA+AES128:EECDH+AES256:RSA+AES256:EECDH+3DES:RSA+3DES:!MD5\",\"ssl-buffer-size\":6,\"ssl-protocol\":\"TLSv1 TLSv1.1 TLSv1.2\"}"));

        result.put(RuleType.SERVER_PROXY_BUFFER_SIZE_RULE.name(), getSystemDefaultSettings(RuleType.SERVER_PROXY_BUFFER_SIZE_RULE, "{\"enabled\":true,\"proxy-buffer-size\":16,\"proxy-buffers-size\":16,\"proxy-buffers-count\":8,\"proxy-busy-buffers-size\":32}"));

        result.put(RuleType.SERVER_HTTP2_CONFIG_RULE.name(), getSystemDefaultSettings(RuleType.SERVER_HTTP2_CONFIG_RULE, "{\"enabled\":false,\"http2-body-preread-size\":64,\"http2-chunk-size\":8,\"http2-idle-timeout\":3,\"http2-max-concurrent-streams\":128,\"http2-max-field-size\":4,\"http2-max-header-size\":16,\"http2-max-requests\":1000,\"http2-recv-buffer-size\":256,\"http2-recv-timeout\":30}"));

        result.put(RuleType.DEFAULT_LISTEN_RULE.name(), getSystemDefaultSettings(RuleType.DEFAULT_LISTEN_RULE, "{\"proxy-protocol\":false,\"http2\":false,\"backlog\":511}"));

        result.put(RuleType.REQUEST_ID_ENABLE.name(), getSystemDefaultSettings(RuleType.REQUEST_ID_ENABLE, "{\"enabled\":false}"));

        result.put(RuleType.ACCESS_BY_LUA.name(), getSystemDefaultSettings(RuleType.ACCESS_BY_LUA, ""));

        result.put(RuleType.REWRITE_BY_LUA.name(), getSystemDefaultSettings(RuleType.REWRITE_BY_LUA, ""));

        result.put(RuleType.PROXY_REQUEST_BUFFER_ENABLE.name(), getSystemDefaultSettings(RuleType.PROXY_REQUEST_BUFFER_ENABLE, ""));

        result.put(RuleType.PROTOCOL_RESPONSE_HEADER.name(), getSystemDefaultSettings(RuleType.PROTOCOL_RESPONSE_HEADER, "{\"enabled\":false}"));

        result.put(RuleType.UPSTREAM_KEEP_ALIVE_TIMEOUT.name(), getSystemDefaultSettings(RuleType.UPSTREAM_KEEP_ALIVE_TIMEOUT, "{\"upstream-keep-alive-timeout\":55}"));

        result.put(RuleType.LARGE_CLIENT_HEADER.name(), getSystemDefaultSettings(RuleType.LARGE_CLIENT_HEADER, "{\"large-client-header-buffers-count\":4,\"large-client-header-buffers-size\":16}"));

        result.put(RuleType.KEEP_ALIVE_TIMEOUT.name(), getSystemDefaultSettings(RuleType.KEEP_ALIVE_TIMEOUT, "{\"client-keep-alive-timeout\":75}"));

        result.put(RuleType.GZIP.name(), getSystemDefaultSettings(RuleType.GZIP, "{\"enabled\":false,\"gzip-min-length\":100,\"gzip-types\":\"text/css\",\"gzip-buffer-count\":16,\"gzip-buffer-size\":8,\"gzip-comp-level\":1}"));

        result.put(RuleType.ENABLE_HSTS.name(), getSystemDefaultSettings(RuleType.ENABLE_HSTS, "{\"enabled\":false,\"hsts-max-age\":3600}"));

        result.put(RuleType.CLIENT_BODY_BUFFER_SIZE.name(), getSystemDefaultSettings(RuleType.CLIENT_BODY_BUFFER_SIZE, "{\"client-body-buffer-size\":16}"));

        result.put(RuleType.SERVER_NAME_HASH.name(), getSystemDefaultSettings(RuleType.SERVER_NAME_HASH, "{\"server-name-hash-max-size\":10000,\"server-name-hash-bucket-size\":128}"));

        return result;
    }

    private String[] getSystemDefaultSettings(RuleType ruleType, String defaultValues) {
        String value = configHandler.getStringValue("system.rule." + ruleType.name(), defaultValues == null ? "" : defaultValues);
        return value.split(";");
    }
}
