package com.ctrip.zeus.restful.message.view;

import java.util.List;

/**
 * Created by zhoumy on 2017/1/18.
 */
public class TrafficPolicyListView extends ListView<ExtendedView.ExtendedTrafficPolicy> {
    public TrafficPolicyListView() {
    }

    public TrafficPolicyListView(int total) {
        super(total);
    }

    public List<ExtendedView.ExtendedTrafficPolicy> getTrafficPolicies() {
        return getList();
    }
}
