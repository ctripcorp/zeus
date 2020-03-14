package com.ctrip.zeus.service.model.impl;

import com.ctrip.zeus.service.model.SelectionMode;

/**
 * Created by zhoumy on 2016/8/26.
 */
public class RepositoryContext {
    private boolean lite;
    private SelectionMode selectionMode;

    public RepositoryContext(boolean lite, SelectionMode selectionMode) {
        this.lite = lite;
        this.selectionMode = selectionMode;
    }

    public boolean isLite() {
        return lite;
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }
}