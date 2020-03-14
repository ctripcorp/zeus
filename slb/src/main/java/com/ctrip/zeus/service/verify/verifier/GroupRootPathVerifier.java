package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.GroupListView;
import com.ctrip.zeus.service.verify.IdItemType;
import com.ctrip.zeus.service.verify.IllegalMarkTypes;
import com.ctrip.zeus.service.verify.VerifyPropertyResult;
import com.ctrip.zeus.service.verify.VerifyResult;
import com.ctrip.zeus.tag.ItemTypes;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Discription
 **/
@Component("groupRootPathVerifier")
public class GroupRootPathVerifier extends AbstractIllegalDataVerifier {

    private final static String ROOT_PATH = "~* ^/";

    @Override
    public List<VerifyResult> verify() throws Exception {
        // todo the logic in tools-probelm.js is quite strange
        List<VerifyResult> results = new ArrayList<>();
        if (this.getContext() != null) {
            GroupListView groupListView = this.getContext().getGroups();
            List<Group> groups = new ArrayList<>(groupListView.getTotal());
            for (ExtendedView.ExtendedGroup extendedGroup : groupListView.getGroups()) {
                groups.add(extendedGroup.getInstance());
            }

            for (Group group : groups) {
                List<Long> illegalPriorityVsIds = allIllegalRootPriorityGvs(group);
                if (illegalPriorityVsIds.size() > 0) {
                    List<IdItemType> idItemTypes = illegalPriorityVsIds.stream().map(vsId -> new IdItemType(vsId, ItemTypes.VS)).collect(Collectors.toList());

                    VerifyPropertyResult result = new VerifyPropertyResult(
                            ItemTypes.GROUP,
                            Collections.singletonList(group.getId()),
                            getMarkName(),
                            PropertyValueUtils.write(idItemTypes));
                    results.add(result);
                }
            }
        }
        return results;
    }

    private List<Long> allIllegalRootPriorityGvs(Group group) {
        List<Long> vsIds = new ArrayList<>();
        if (group != null) {
            for (GroupVirtualServer groupVirtualServer : group.getGroupVirtualServers()) {
                if (ROOT_PATH.equals(groupVirtualServer.getPath()) && groupVirtualServer.getPriority() > -1000) {
                    vsIds.add(groupVirtualServer.getVirtualServer().getId());
                }
            }
        }
        return vsIds;
    }

    @Override
    public String getMarkName() {
        return "ILLEGAL_ROOT_PATH_PRIORITY";
    }

    @Override
    public String getTargetItemType() {
        return ItemTypes.GROUP;
    }

    @Override
    public String getMarkType() {
        return IllegalMarkTypes.PROPERTY;
    }

    @Override
    public String getDisplayName() {
        return "not-proper-root-path-priority";
    }
}
