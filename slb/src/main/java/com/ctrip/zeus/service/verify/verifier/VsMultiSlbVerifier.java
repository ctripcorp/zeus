package com.ctrip.zeus.service.verify.verifier;

import com.ctrip.zeus.restful.message.view.ExtendedView;
import com.ctrip.zeus.service.verify.IllegalMarkTypes;
import com.ctrip.zeus.service.verify.VerifyContext;
import com.ctrip.zeus.service.verify.VerifyResult;
import com.ctrip.zeus.service.verify.VerifyTaggingResult;
import com.ctrip.zeus.tag.ItemTypes;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Discription
 **/
@Component("vsMultiSlbVerifier")
public class VsMultiSlbVerifier extends AbstractIllegalDataVerifier {
    @Override
    public List<VerifyResult> verify() throws Exception {
        if (getContext() != null) {
            VerifyContext context = getContext();
            List<ExtendedView.ExtendedVs> extendedVses = context.getVses().getVirtualServers();
            List<Long> vsIds = Lists.transform(
                    extendedVses.stream().filter(this::hasMultiSlb).collect(Collectors.toList()),
                    ExtendedView.ExtendedVs::getId
            );
            return Arrays.asList(new VerifyTaggingResult(ItemTypes.VS, vsIds, getMarkName()));
        }
        return new ArrayList<>();
    }

    private boolean hasMultiSlb(ExtendedView.ExtendedVs extendedVs) {
        if (extendedVs != null) {
            return extendedVs.getInstance().getSlbIds().size() > 1;
        }
        return false;
    }

    @Override
    public String getMarkName() {
        return "ILLEGAL_VS_MULTI_SLB";
    }

    @Override
    public String getTargetItemType() {
        return ItemTypes.VS;
    }

    @Override
    public String getMarkType() {
        return IllegalMarkTypes.TAG;
    }

    @Override
    public String getDisplayName() {
        return "illegal-vs-multiple-slb";
    }
}
