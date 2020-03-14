package com.ctrip.zeus.restful.message.view;

import java.util.List;

public class RuleListView extends ListView<ExtendedView.ExtendedRule> {
    public RuleListView() {
    }

    public RuleListView(int total) {
        super(total);
    }

    public List<ExtendedView.ExtendedRule> getRules() {
        return getList();
    }
}
