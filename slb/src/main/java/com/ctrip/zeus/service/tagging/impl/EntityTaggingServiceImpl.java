package com.ctrip.zeus.service.tagging.impl;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.tagging.EntityTaggingService;
import com.ctrip.zeus.tag.ItemTypes;
import com.ctrip.zeus.tag.TagBox;
import com.ctrip.zeus.tag.TagNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author dongyq
 */
@Service
public class EntityTaggingServiceImpl implements EntityTaggingService {

    @Autowired
    GroupRepository groupRepository;
    @Resource
    TagBox tagBox;

    @Override
    public void tagGroupAdvancedFeatures(Long groupId) throws Exception {
        Group group = groupRepository.getById(groupId);
        if (group == null) {
            return;
        }
        tagGroupAdvancedFeatures(group);
    }

    @Override
    public void tagGroupAdvancedFeatures(Group group) throws Exception {
        boolean hasExtendedRouting = false, hasCustomConf = false, hasName = false;
        if (group.getGroupVirtualServers() != null) {
            for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                if (gvs.getRouteRules() != null && !gvs.getRouteRules().isEmpty()) {
                    hasExtendedRouting = true;
                }
                if (gvs.getCustomConf() != null && !gvs.getCustomConf().isEmpty()) {
                    hasCustomConf = true;
                }
                if (gvs.getName() != null && !gvs.getName().isEmpty()) {
                    hasName = true;
                }
            }
        }

        Long[] groupIdArray = new Long[] {group.getId()};
        if (hasExtendedRouting) {
            tagBox.tagging(TagNames.EXTENDED_ROUTING, ItemTypes.GROUP, groupIdArray);
        } else {
            tagBox.untagging(TagNames.EXTENDED_ROUTING, ItemTypes.GROUP, groupIdArray);
        }
        if (hasCustomConf) {
            tagBox.tagging(TagNames.CUSTOM_CONF, ItemTypes.GROUP, groupIdArray);
        } else {
            tagBox.untagging(TagNames.CUSTOM_CONF, ItemTypes.GROUP, groupIdArray);
        }
        if (hasName) {
            tagBox.tagging(TagNames.NAMED_LOCATION, ItemTypes.GROUP, groupIdArray);
        } else {
            tagBox.untagging(TagNames.NAMED_LOCATION, ItemTypes.GROUP, groupIdArray);
        }
    }
}
