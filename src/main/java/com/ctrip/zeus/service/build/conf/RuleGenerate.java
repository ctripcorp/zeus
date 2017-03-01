package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Rule;

/**
 * Created by zhoumy on 2017/2/24.
 */
public interface RuleGenerate {

    String generateCommandValue(Rule rule) throws Exception;
}