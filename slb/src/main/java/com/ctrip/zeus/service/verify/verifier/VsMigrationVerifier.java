package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.model.model.VirtualServer;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.GroupListView;
import com.ctrip.zeus.restful.message.view.VsListView;
import com.ctrip.zeus.service.verify.*;
import com.ctrip.zeus.tag.ItemTypes;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @Discription
 **/
@Component("vsMigrationVerifier")
public class VsMigrationVerifier extends AbstractIllegalDataVerifier {

    private static final String ROOT_PATH = "~* ^/";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List<VerifyResult> verify() throws Exception {
        List<VerifyResult> results = new ArrayList<>();
        VerifyContext context = getContext();
        if (context != null) {
            VsListView vsListView = context.getVses();
            Map<Long, VirtualServer> vsIdVsMap = new HashMap<>(vsListView.getTotal());
            for (ExtendedView.ExtendedVs extendedVs : vsListView.getVirtualServers()) {
                vsIdVsMap.put(extendedVs.getId(), extendedVs.getInstance());
            }

            GroupListView groupListView = context.getGroups();
            List<Group> groups = Lists.transform(groupListView.getList(), ExtendedView.ExtendedGroup::getInstance);
            Map<Long, List<Group>> vsIdGroupsMap = groupBy(groups, group -> {
                if (group.getGroupVirtualServers() != null && group.getGroupVirtualServers().size() > 0) {
                    return Lists.transform(group.getGroupVirtualServers(), gvs -> gvs.getVirtualServer().getId());
                }
                return new ArrayList<>();
            });

            for (Long vsId : vsIdGroupsMap.keySet()) {
                VirtualServer vs = vsIdVsMap.get(vsId);
                List<Long> groupIds = allGroupIllegalForMigration(vs, vsIdGroupsMap.get(vs.getId()));
                if (groupIds.size() > 0) {
                    List<IdItemType> idItemTypes = Lists.transform(groupIds, groupId -> new IdItemType(groupId, ItemTypes.GROUP));
                    results.add(new VerifyPropertyResult(
                            getTargetItemType(),
                            Collections.singletonList(vs.getId()),
                            getMarkName(),
                            PropertyValueUtils.write(idItemTypes)));
                }
            }
        }
        return results;
    }

    private List<Long> allGroupIllegalForMigration(VirtualServer vs, List<Group> groups) {
        List<Long> groupIds = new ArrayList<>();
        // 1. find root path's priority. assert only one root path group
        // 2. find all groups that whose path priority is <= root_priority + 1;

        if (groups != null) {
            Map<String, Group> pathGroupMap = new HashMap<>();
            Map<String, GroupVirtualServer> pathGvsMap = new HashMap<>();
            // build path-group map
            for (Group group : groups) {
                // find corresponding gvs with vs
                GroupVirtualServer targetGvs = null;
                for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                    if (vs.getId().equals(gvs.getVirtualServer().getId())) {
                        targetGvs = gvs;
                        break;
                    }
                }
                if (targetGvs != null) {
                    if (pathGroupMap.containsKey(targetGvs.getPath())) {
                        logger.warn("Duplicate paths found in one vs belonged to different groups. Group id: "
                                + pathGroupMap.get(targetGvs.getPath()).getId() + ", " + group.getId());
                    } else {
                        pathGroupMap.put(targetGvs.getPath(), group);
                        pathGvsMap.put(targetGvs.getPath(), targetGvs);
                    }
                }
            }

            if (pathGroupMap.containsKey(ROOT_PATH)) {
                int rootPriority = pathGvsMap.get(ROOT_PATH).getPriority();
                pathGroupMap.remove(ROOT_PATH);
                for (String path : pathGroupMap.keySet()) {
                    if (rootPriority + 1 >= pathGvsMap.get(path).getPriority()) {
                        groupIds.add(pathGroupMap.get(path).getId());
                    }
                }
            }
        }

        return groupIds;
    }

    @Override
    public String getMarkName() {
        return "ILLEGAL_FOR_MIGRATION";
    }

    @Override
    public String getTargetItemType() {
        return ItemTypes.VS;
    }

    @Override
    public String getMarkType() {
        return IllegalMarkTypes.PROPERTY;
    }

    @Override
    public String getDisplayName() {
        return "vs-illegal-for-migration";
    }
}
