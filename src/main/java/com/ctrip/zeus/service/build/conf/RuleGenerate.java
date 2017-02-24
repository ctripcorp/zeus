package com.ctrip.zeus.service.build.conf;

/**
 * Created by zhoumy on 2017/2/24.
 */
public interface RuleGenerate {

    void generateRuleCommand(ConfWriter confWriter);
}