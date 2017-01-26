package com.ctrip.zeus.service.model.common;

import com.ctrip.zeus.service.model.grammar.GrammarException;

import java.util.List;

/**
 * Created by zhoumy on 2017/1/25.
 */
public class ValidationContext {
    private List<String> errors;

    public void error(Long entryId, MetaType entryType, String cause) {

    }
}
