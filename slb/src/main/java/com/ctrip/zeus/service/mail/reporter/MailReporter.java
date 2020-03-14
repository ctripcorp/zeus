package com.ctrip.zeus.service.mail.reporter;

/**
 * Created by fanqq on 2017/3/21.
 */
public interface MailReporter {
    boolean should();
    void report() throws Exception;
}
