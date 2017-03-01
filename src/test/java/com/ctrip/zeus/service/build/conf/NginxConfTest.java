package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.model.entity.Rule;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.service.model.common.RulePhase;
import com.ctrip.zeus.service.model.common.RuleSet;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zhoumy on 2017/2/24.
 */
public class NginxConfTest {
    @Test
    public void testGenerateRuleConf() {
        Slb slb = new Slb().setId(1L).addRule(new Rule().setPhaseId(RulePhase.HTTP_INIT_BY_LUA.getId()).setName("init_randomseed"));

        NginxConf nginxConf = new NginxConf();
        ConfWriter confWriter = new ConfWriter();
        RuleSet<Slb> generationRules = new RuleSet<>(slb);
        for (Rule rule : slb.getRuleSet()) {
            generationRules.addRule(rule);
        }
        try {
            confWriter.writeCommand("init_by_lua", nginxConf.generateLuaInitScripts(true, generationRules.getRules()));
            System.out.println(confWriter.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

        confWriter = new ConfWriter();
        try {
            confWriter.writeCommand("init_by_lua", nginxConf.generateLuaInitScripts(false, generationRules.getRules()));
            System.out.println(confWriter.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }
}
