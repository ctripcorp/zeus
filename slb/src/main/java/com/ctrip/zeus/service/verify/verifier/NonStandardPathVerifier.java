package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.model.Group;
import com.ctrip.zeus.model.model.GroupVirtualServer;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.verify.*;
import com.ctrip.zeus.tag.ItemTypes;
import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @Discription
 **/
@Component("nonStandardPathVerifier")
public class NonStandardPathVerifier extends AbstractIllegalDataVerifier {

    @Override
    public List<VerifyResult> verify() throws Exception {
        List<VerifyResult> results = new ArrayList<>();

        VerifyContext context = getContext();
        if (context != null && context.getGroups() != null) {
            for (ExtendedView.ExtendedGroup extendedGroup : context.getGroups().getGroups()) {
                Group group = extendedGroup.getInstance();
                List<Long> nonStandardVsIds = new LinkedList<>();
                for (GroupVirtualServer gvs : group.getGroupVirtualServers()) {
                    if (!isPathStandard(gvs.getPath())) {
                        nonStandardVsIds.add(gvs.getVirtualServer().getId());
                    }
                }
                if (nonStandardVsIds.size() == 0) {
                    continue;
                }

                List<IdItemType> vsIds = new ArrayList<>(nonStandardVsIds.size());
                for (Long vsId : nonStandardVsIds) {
                    vsIds.add(new IdItemType(vsId, ItemTypes.VS));
                }
                results.add(new VerifyPropertyResult(getTargetItemType(), Collections.singletonList(group.getId()), getMarkName(), PropertyValueUtils.write(vsIds)));
            }
        }
        return results;
    }

    private boolean isPathStandard(String path) {
        if (Strings.isNullOrEmpty(path)) {
            return true;
        }
        String prefix = "~* ^/";
        String suffix = "($|/|\\?)";

        // rm prefix and suffix from path
        int prefixIdx = path.indexOf(prefix);
        if (prefixIdx != -1) {
            path = path.substring(prefixIdx + prefix.length());
        }
        int suffixIdx = path.indexOf(suffix);
        if (suffixIdx != -1) {
            path = path.substring(0, suffixIdx);
        }

        String[] illegalTokens = {"|", "*", "+", ".", "[", "]", "\\", "(", ")"};
        for (String token : illegalTokens) {
            if (path.contains(token)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getMarkName() {
        return "ILLEGAL_NON_STANDARD_PATH";
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
        return "non-standard-path";
    }
}
