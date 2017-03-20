package com.ctrip.zeus.service.mail.impl;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Properties;

/**
 * Created by fanqq on 2017/3/20.
 */
@Service("defaultMailServiceImpl")
public class DefaultMailServiceImpl implements MailService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private DynamicStringProperty host = DynamicPropertyFactory.getInstance().getStringProperty("mail.smtp.host", null);
    private DynamicStringProperty sender = DynamicPropertyFactory.getInstance().getStringProperty("mail.sender", null);

    @Override
    public void sendEmail(SlbMail mail) throws Exception {

        if (mail.getSubject() == null || mail.getSubject().isEmpty()) {
            throw new IllegalArgumentException("Mail subject can not be blank!");
        }

        if (mail.getRecipients() == null || mail.getRecipients().size() == 0) {
            throw new IllegalArgumentException("Mail receivers can not be empty!");
        }
        if (host.get() == null || sender.get() == null) {
            throw new ValidationException("Mail host and sender is not configured!");
        }

        Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", host.get());
        properties.setProperty("mail.smtp.auth", "false");
        properties.put("mail.smtp.socketFactory.fallback", "true");
        Session session = Session.getInstance(properties);
        session.setDebug(false);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("\"" + MimeUtility.encodeText(sender.get().split("@")[0]) + "\"<" + sender.get() + ">"));
        msg.setSubject(mail.getSubject());
        Multipart bodyMultipart = new MimeMultipart("related");
        msg.setContent(bodyMultipart);
        BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(mail.getBody(), "text/html;charset=utf-8");
        bodyMultipart.addBodyPart(htmlPart);
        msg.saveChanges();

        List<String> receivers = mail.getRecipients();
        Address[] tos = new InternetAddress[receivers.size()];
        for (int i = 0, j = receivers.size(); i < j; i++) {
            tos[i] = new InternetAddress(receivers.get(i));
        }
        msg.setRecipients(Message.RecipientType.TO, tos);
        Transport.send(msg);
    }
}
