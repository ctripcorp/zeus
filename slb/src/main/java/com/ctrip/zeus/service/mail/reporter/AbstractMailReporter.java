package com.ctrip.zeus.service.mail.reporter;

import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by fanqq on 2017/3/21.
 */
@Service
public abstract class AbstractMailReporter implements MailReporter {
    @Resource
    private MailReporterManager mailReporterManager;

    protected DynamicStringProperty slbTeamMailAddr = DynamicPropertyFactory.getInstance().getStringProperty("slb.team.mail", null);


    @PostConstruct
    protected void init() {
        mailReporterManager.addReporter(this);
    }

}
