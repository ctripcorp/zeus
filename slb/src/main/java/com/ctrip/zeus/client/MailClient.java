package com.ctrip.zeus.client;

import com.ctrip.zeus.service.mail.model.MailEntity;
import com.ctrip.zeus.support.DefaultObjectJsonWriter;
import com.netflix.config.DynamicPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

/**
 * Created by fanqq on 2017/3/20.
 */
public class MailClient extends AbstractRestClient {
    private static MailClient instance = null;
    private static final String mailhost = DynamicPropertyFactory.getInstance().getStringProperty("mail.host", "http://localhost").get();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected MailClient(String url) {
        super(url);
    }

    public static MailClient getInstance() {
        if (instance == null) {
            instance = new MailClient(mailhost);
        }
        return instance;
    }

    public boolean sendMail(MailEntity entity) {
        String msg = null;
        try {
            msg = DefaultObjectJsonWriter.write(entity);
            String res = getTarget().path("/json/SendEmail").request().post(Entity.entity(msg, MediaType.APPLICATION_JSON_TYPE), String.class);
            logger.info("Send Mail Success. Response:" + res);
            return true;
        } catch (Exception e) {
            logger.error("Send Mail Failed. Send Message Detail:" + msg);
            return false;
        }

    }
}
