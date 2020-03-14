package com.ctrip.zeus.service.mail.reporter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fanqq on 2017/3/21.
 */
@Service("mailReporterManager")
public class MailReporterManager {

    private List<MailReporter> reporters = new ArrayList<>();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void addReporter(MailReporter reporter) {
        reporters.add(reporter);
    }

    public void run() {
        for (MailReporter reporter : reporters) {
            if (reporter.should()) {
                try {
                    reporter.report();
                } catch (Exception e) {
                    logger.error("Mail Reporter Execute Failed. Reporter:" + reporter.getClass().getSimpleName(), e);
                }
            }
        }
    }
}
