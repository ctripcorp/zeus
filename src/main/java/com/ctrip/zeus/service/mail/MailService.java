package com.ctrip.zeus.service.mail;

import com.ctrip.zeus.service.mail.model.SlbMail;

/**
 * Created by fanqq on 2017/3/17.
 */
public interface MailService {
    public void sendEmail(SlbMail mail) throws Exception;
}
