package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.model.Property;
import com.ctrip.zeus.model.model.Domain;
import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.restful.message.view.VsListView;
import com.ctrip.zeus.service.verify.*;
import com.ctrip.zeus.tag.ItemTypes;
import com.google.common.collect.Sets;
import org.jvnet.hk2.component.MultiMap;
import org.springframework.stereotype.Component;

import java.util.*;

;

/**
 * @Discription
 **/
@Component("vsDuplicateDomainVerifier")
public class VsDuplicateDomainVerifier extends AbstractIllegalDataVerifier {

    @Override
    public List<VerifyResult> verify() throws Exception {
        VerifyContext verifyContext = getContext();
        List<VerifyResult> results = new ArrayList<>();
        if (verifyContext != null) {
            VsListView vsListView = verifyContext.getVses();
            MultiMap<VsKey, ExtendedView.ExtendedVs> vsMap = new MultiMap<>();
            for (ExtendedView.ExtendedVs vs : vsListView.getVirtualServers()) {
                List<VsKey> keys = getKeys(vs);
                for (VsKey key : keys) {
                    vsMap.add(key, vs);
                }
            }

            for (VsKey key : vsMap.keySet()) {
                List<ExtendedView.ExtendedVs> extendedVses = vsMap.get(key);
                List<List<Long>> vsIdsList = getDomainOverlapVsIds(extendedVses);
                for (List<Long> vsIds : vsIdsList) {
                    if (vsIds != null && vsIds.size() > 0) {
                        assert vsIds.size() == 2;
                        results.add(new VerifyPropertyResult(
                                getTargetItemType(),
                                Collections.singletonList(vsIds.get(0)),
                                getMarkName(),
                                PropertyValueUtils.write(Collections.singletonList(new IdItemType(vsIds.get(1), getTargetItemType())))));
                    }
                }
            }
        }
        return results;
    }

    private List<List<Long>> getDomainOverlapVsIds(List<ExtendedView.ExtendedVs> extendedVses) {
        List<List<Long>> results = new ArrayList<>();
        if (extendedVses != null) {
            for (int i = 0; i != extendedVses.size(); i++) {
                for (int j = i + 1; j != extendedVses.size(); j++) {
                    ExtendedView.ExtendedVs item = extendedVses.get(i);
                    ExtendedView.ExtendedVs anotherItem = extendedVses.get(j);

                    Set<Domain> domains1 = new HashSet<>(item.getInstance().getDomains());
                    Set<Domain> domains2 = new HashSet<>(anotherItem.getInstance().getDomains());
                    if (!Sets.intersection(domains1, domains2).isEmpty()) {
                        results.add(Arrays.asList(item.getId(), anotherItem.getId()));
                    }
                }
            }
        }
        return results;
    }

    private List<VsKey> getKeys(ExtendedView.ExtendedVs extendedVs) throws Exception {
        // iterate over all slbId, make a key with slbId, idc and ssl
        List<VsKey> keys = new ArrayList<>();
        List<Long> slbIds = extendedVs.getInstance().getSlbIds();

        for (Long slbId : slbIds) {
            String idc = getSlbIdc(slbId);
            if (idc == null) {
                throw new RuntimeException("Idc property not exists for slbId : " + slbId);
            }
            keys.add(new VsKey(idc, extendedVs.getInstance().getSsl(), slbId));
        }
        return keys;
    }

    private String getSlbIdc(Long slbId) {
        ExtendedView.ExtendedSlb targetSlb = null;
        for (ExtendedView.ExtendedSlb slb : getContext().getSlbs().getSlbs()) {
            if (slbId.equals(slb.getId())) {
                targetSlb = slb;
                break;
            }
        }
        if (targetSlb == null) {
            throw new RuntimeException("Slb can not be found for slbId : " + slbId);
        }
        for (Property property : targetSlb.getProperties()) {
            if ("idc".equalsIgnoreCase(property.getName())) {
                return property.getValue();
            }
        }
        return null;
    }

    @Override
    public String getMarkName() {
        return "OVERLAP_DOMAIN_IN_SAME_IDC_SLB_SSL";
    }

    private static class VsKey {
        // the key to group vs by
        private final String idc;
        private final Boolean ssl;
        private final Long slbId;

        public VsKey(String idc, Boolean ssl, Long slbId) {
            this.idc = idc;
            this.ssl = ssl;
            this.slbId = slbId;
        }

        public String getIdc() {
            return idc;
        }

        public Boolean getSsl() {
            return ssl;
        }

        public Long getSlbId() {
            return slbId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VsKey)) return false;
            VsKey key = (VsKey) o;
            return Objects.equals(getIdc(), key.getIdc()) &&
                    Objects.equals(getSsl(), key.getSsl()) &&
                    Objects.equals(getSlbId(), key.getSlbId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getIdc(), getSsl(), getSlbId());
        }
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
        return "vs-overlap-domains";
    }
}
