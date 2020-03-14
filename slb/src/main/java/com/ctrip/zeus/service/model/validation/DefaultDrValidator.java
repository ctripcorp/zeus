package com.ctrip.zeus.service.model.validation;

import com.ctrip.zeus.dao.entity.SlbDrStatusR;
import com.ctrip.zeus.dao.entity.SlbDrStatusRExample;
import com.ctrip.zeus.dao.mapper.SlbDrStatusRMapper;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.status.GroupServerStatus;
import com.ctrip.zeus.model.status.GroupStatus;
import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.status.GroupStatusService;
import com.ctrip.zeus.tag.PropertyService;
import com.ctrip.zeus.tag.TagService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

;

@Component("drValidator")
public class DefaultDrValidator implements DrValidator {
    @Resource
    private PropertyService propertyService;

    @Resource
    private SlbDrStatusRMapper slbDrStatusRMapper;

    @Resource
    private TagService tagService;

    @Resource
    private GroupStatusService groupStatusService;

    @Resource
    private ConfigHandler configHandler;

    private final static String GROUPID_PREFIX = "groupid_";

    @Override
    public void checkRestrictionForUpdate(Dr target) throws Exception {
        SlbDrStatusR check = slbDrStatusRMapper.selectOneByExample(new SlbDrStatusRExample().createCriteria().andDrIdEqualTo(target.getId()).example());
        if (check == null) {
            throw new ValidationException("Dr that you try to update does not exist.");
        }
        if (check.getOfflineVersion() > target.getVersion()) {
            throw new ValidationException("Newer version is detected.");
        }
        if (!check.getOfflineVersion().equals(target.getVersion())) {
            throw new ValidationException("Incompatible version.");
        }
    }

    @Override
    public void removable(Long targetId) throws Exception {
        SlbDrStatusR check = slbDrStatusRMapper.selectOneByExample(new SlbDrStatusRExample().createCriteria().andDrIdEqualTo(targetId).example());
        if (check == null) return;

        if (check.getOnlineVersion() != 0) {
            throw new ValidationException("Dr that you try to delete is still active.");
        }
    }

    @Override
    public void validateFields(Dr dr, ValidationContext context) throws ValidationException {
        if (dr.getName() == null || dr.getName().isEmpty()) {
            throw new ValidationException("Field `name` is not allowed empty.");
        }

        checkDuplicates(dr.getDrTraffics(), false, new Comparator<DrTraffic>() {
            @Override
            public int compare(DrTraffic o1, DrTraffic o2) {
                return o1.getGroup().getId().compareTo(o2.getGroup().getId());
            }
        });
        for (DrTraffic traffic : dr.getDrTraffics()) {
            checkDuplicates(traffic.getDestinations(), true, new Comparator<Destination>() {
                @Override
                public int compare(Destination o1, Destination o2) {
                    return o1.getVirtualServer().getId().compareTo(o2.getVirtualServer().getId());
                }
            });
            for (Destination destination : traffic.getDestinations()) {
                checkDuplicates(destination.getControls(), false, new Comparator<TrafficControl>() {
                    @Override
                    public int compare(TrafficControl o1, TrafficControl o2) {
                        return o1.getGroup().getId().compareTo(o2.getGroup().getId());
                    }
                });
                int totalWeight = 0;
                for (TrafficControl control : destination.getControls()) {
                    if (control.getWeight() <= 0) {
                        throw new ValidationException("Field `weight` only accepts positive value.");
                    }
                    totalWeight += control.getWeight();
                }
                if (totalWeight > 100) {
                    throw new ValidationException("Sum of field `weight` can not exceed 100.");
                }
            }
        }
    }

    @Override
    public void checkGroupRelations(Dr dr, Set<Long> sourceGroupIds, Set<Long> desGroupIds) throws Exception {
        if (!sourceGroupIds.containsAll(desGroupIds)) {
            throw new ValidationException("destination group must be a subset of source groups!");
        }
        List<String> tagNames = new ArrayList<>();
        for (Long groupId : sourceGroupIds) {
            tagNames.add(GROUPID_PREFIX + groupId);
        }
        Set<Long> check = tagService.unionQuery(tagNames, "dr");

        if (dr.getId() == null) {
            //new dr
            if (check != null && check.size() > 0) {
                throw new ValidationException("groups already have a related dr! Id:" + check.iterator().next());
            }
        } else {
            //update
            if (check != null) {
                for (Long id : check) {
                    if (!dr.getId().equals(id)) {
                        List<String> tags = tagService.getTags("dr", id);
                        Set<Long> drGroups = new HashSet<>();
                        for (String tag : tags) {
                            drGroups.add(Long.parseLong(tag.split("_")[1]));
                        }
                        sourceGroupIds.retainAll(drGroups);
                        throw new ValidationException("group: " + sourceGroupIds.toString() + " already have a related dr! Id:" + id);
                    }
                }
            }
        }
    }

    @Override
    public void checkGroupAvailability(Set<Long> groupIds) throws Exception {
        List<GroupStatus> statusList = groupStatusService.getOfflineGroupsStatus(groupIds);
        for (GroupStatus groupStatus : statusList) {
            Long gid = groupStatus.getGroupId();
            int total = groupStatus.getGroupServerStatuses().size();
            if (total == 0) {
                throw new ValidationException("No available server in group: " + gid);
            }
            int upCount = 0;
            for (GroupServerStatus gss : groupStatus.getGroupServerStatuses()) {
                if (gss.isUp()) {
                    upCount++;
                }
            }
            int minThreshHold = configHandler.getIntValue("dr.group.available.threshold", null, null, gid, 1);
            if (minThreshHold > 100) minThreshHold = 100;
            if (upCount * 100 / total < minThreshHold) {
                throw new ValidationException("up servers in target group[" + gid + "] are below minimum threshold: " + minThreshHold);
            }
        }
    }

    @Override
    public void checkDrProperties(Set<Long> sourceGroupIds) throws ValidationException {
        Map<Long, Property> propertyMap;
        try {
            propertyMap = propertyService.getProperties("slbDrId", "group", sourceGroupIds.toArray(new Long[sourceGroupIds.size()]));
        } catch (Exception e) {
            throw new ValidationException("fetch slbDrId failed");
        }
        String pvalue = null;
        for (Long sourceGroupId : sourceGroupIds) {
            Property val = propertyMap.get(sourceGroupId);
            if (val == null || val.getValue() == null) {
                throw new ValidationException("group: " + sourceGroupId + " does not have property: slbDrId");
            }
            if (pvalue == null) {
                pvalue = val.getValue();
            } else if (!pvalue.equalsIgnoreCase(val.getValue())) {
                throw new ValidationException("multiple slbDrId found (" + pvalue + ", " + val.getValue() + "), groups should have the same slbDrId");
            }
        }
    }

    @Override
    public List<Node> checkGroupsAndVses(Dr dr, Map<Long, Group> drRelatedGroups, Map<Long, VirtualServer> vsLookup, Map<Long, Property> slbIdcInfo, Map<Long, Property> groupIdcInfo) throws Exception {
        List<Node> nodes = new ArrayList<>();
        for (DrTraffic traffic : dr.getDrTraffics()) {
            Long sourceGroupId = traffic.getGroup().getId();
            for (Destination des : traffic.getDestinations()) {
                Long sourceVsId = des.getVirtualServer().getId();
                for (TrafficControl control : des.getControls()) {
                    VirtualServer desVs = checkAndGetDesVs(drRelatedGroups.get(sourceGroupId), drRelatedGroups.get(control.getGroup().getId()), vsLookup.get(sourceVsId), vsLookup);
                    checkAndGetDesSlbId(control.getGroup().getId(), desVs, slbIdcInfo, groupIdcInfo);
                    nodes.add(new Node(sourceGroupId + ";" + sourceVsId, control.getGroup().getId() + ";" + desVs.getId()));
                }
            }
        }
        return nodes;
    }

    @Override
    public VirtualServer checkAndGetDesVs(Group sourceGroup, Group desGroup, VirtualServer vs1, Map<Long, VirtualServer> vsLookup) throws
            ValidationException {
        //1. check if target exists
        if (sourceGroup == null || desGroup == null || vs1 == null) {
            throw new ValidationException("dr related group or vs not found!");
        }

        // 2. find target gvs that has same path/ssl/port/rewrite/domains etc.
        GroupVirtualServer gvs1 = null;
        for (GroupVirtualServer gvs : sourceGroup.getGroupVirtualServers()) {
            if (vs1.getId().equals(gvs.getVirtualServer().getId())) {
                gvs1 = gvs;
                break;
            }
        }
        if (gvs1 == null) {
            throw new ValidationException("group virtual server not found for groupId: " + sourceGroup.getId() + ", vsId: " + vs1.getId());
        }

        VirtualServer vs2 = null;
        List<VirtualServer> matchVs = new ArrayList<>();
        for (GroupVirtualServer gvs : desGroup.getGroupVirtualServers()) {
            vs2 = vsLookup.get(gvs.getVirtualServer().getId());
            if (vs2 == null) continue;
            if (compare(vs1.getId(), vs2.getId())) continue;
            if (!compare(gvs1.getPath(), gvs.getPath())) continue;
            if (!compare(gvs1.getRewrite(), gvs.getRewrite())) continue;
            if (!compare(vs1.getSsl(), vs2.getSsl())) continue;
            if (!compare(vs1.getPort(), vs2.getPort())) continue;

            Set<String> domains1 = new HashSet<>();
            Set<String> domains2 = new HashSet<>();
            for (Domain d : vs1.getDomains()) {
                domains1.add(d.getName());
            }
            for (Domain d : vs2.getDomains()) {
                domains2.add(d.getName());
            }
            if (domains1.equals(domains2)) {
                matchVs.add(vs2);
            }
        }
        if (matchVs.size() == 0) {
            throw new ValidationException("group virtual server not found for groupId: " + desGroup.getId() + ", matching sourceVsId: " + vs1.getId());
        }
        if (matchVs.size() > 1) {
            throw new ValidationException("too many group virtual server found for groupId: " + desGroup.getId() + ", sourceVsId: " + vs1.getId());
        }
        return matchVs.get(0);
    }

    private boolean compare(Object obj1, Object obj2) {
        if (obj1 == null) return obj2 == null;
        return obj1.equals(obj2);
    }

    @Override
    public Long checkAndGetDesSlbId(Long desGroupId, VirtualServer desVs, Map<Long, Property> slbIdcInfo, Map<Long, Property> groupIdcInfo) throws Exception {
        Property p = groupIdcInfo.get(desGroupId);
        if (p == null) {
            throw new ValidationException("Idc info not found for groupId: " + desGroupId);
        }
        String idc = p.getValue();
        if (idc == null || idc.isEmpty()) {
            throw new ValidationException("Empty Idc info for groupId: " + desGroupId);
        }
        List<Long> result = new ArrayList<>();
        for (Long slbId : desVs.getSlbIds()) {
            Property tmp = slbIdcInfo.get(slbId);
            if (tmp != null && idc.equalsIgnoreCase(tmp.getValue())) result.add(slbId);
        }
        if (result.size() >= 1) {
            Collections.sort(result);
            return result.get(0);
        } else {
            throw new ValidationException("No matching slb found at: " + idc + " for desGroupId: " + desGroupId);
        }
    }

    @Override
    public void validateLoop(List<Node> nodes) throws ValidationException {
        Map<String, List<String>> graph = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Stack<String> stack = new Stack<>();
        for (Node node : nodes) {
            List<String> targets = graph.get(node.source);
            if (targets == null) {
                targets = new ArrayList<>();
                graph.put(node.source, targets);
            }
            targets.add(node.des);
        }
        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                dfs(node, graph, visited, stack);
            }
        }
    }

    private void dfs(String node, Map<String, List<String>> graph, Set<String> visited, Stack<String> stack) throws ValidationException {
        visited.add(node);
        stack.push(node);
        if (graph.containsKey(node)) {
            for (String des : graph.get(node)) {
                if (stack.contains(des)) {
                    throw new ValidationException("find loop in Dr, gvsIds: " + stack.subList(stack.indexOf(des), stack.size()));
                } else {
                    dfs(des, graph, visited, stack);
                }
            }
        }
        stack.pop();
    }

    private <T> void checkDuplicates(List<T> list, boolean allowEmpty, Comparator<T> comparator) throws
            ValidationException {
        if (list.isEmpty()) {
            if (!allowEmpty) {
                throw new ValidationException("collection not allowed empty");
            } else {
                return;
            }
        }
        Collections.sort(list, comparator);

        Iterator<T> iter = list.iterator();
        T prev = iter.next();
        while (iter.hasNext()) {
            T current = iter.next();
            if (comparator.compare(prev, current) == 0) {
                throw new ValidationException("duplicate " + current.getClass().getSimpleName() + " found!");
            }
            prev = current;
        }
    }
}
