package com.ctrip.zeus.service.mail.impl;

import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.service.mail.MailService;
import com.ctrip.zeus.service.mail.model.SlbMail;
import com.ctrip.zeus.util.ObjectJsonWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @Discription: used in open-source version
 **/
@Service("smtpMailServiceImpl")
public class SmtpMailServiceImpl implements MailService {

    @Resource
    private ConfigHandler configHandler;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private BlockingQueue<SlbMail> queue;
    private SendMailThread sendMailThread;
    private DynamicIntProperty queueSize = DynamicPropertyFactory.getInstance().getIntProperty("smtp.mail.service.queue.size", 1000);

    public SmtpMailServiceImpl() {
        queue = new ArrayBlockingQueue<>(queueSize.get());
        sendMailThread = new SendMailThread(queue);
        sendMailThread.start();
    }

    @Override
    public void sendEmail(SlbMail mail) throws Exception {
        logger.info("[[SmtpMail=queue]]Add Mail To Send Queue.MailData:" + ObjectJsonWriter.write(mail) + ";QueueSize:" + queue.size());
        if (mail == null || mail.getSubject() == null || mail.getRecipients() == null || mail.getRecipients().size() == 0) {
            logger.warn("[[SmtpMail=queue]]Ignored Empty Mail Message.");
            return;
        }
        queue.add(mail);
        logger.info("[[SmtpMail=queue]]Added Mail To Send Queue.MailData:" + ObjectJsonWriter.write(mail) + ";QueueSize:" + queue.size());
    }

    class SendMailThread extends Thread {
        BlockingQueue<SlbMail> queue = null;
        int count = 0;

        SendMailThread(BlockingQueue<SlbMail> queue) {
            this.queue = queue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (configHandler == null) {
                        // sleep a while to wait configHandler to be initiated
                        Thread.sleep(10000);
                        logger.warn("[SendMailThread] configHandler not initiated");
                        continue;
                    }

                    if (queue.size() > 0) {
                        try {
                            sendMail(queue.poll());
                            count++;
                        } catch (JsonProcessingException e) {
                            logger.error("MailData Parser Failed.", e);
                        }
                        if (count % configHandler.getIntValue("smtp.mail.service.busy.sleep.interval", 20) == 0) {
                            logger.info("[[SmtpMail=queue]]QueueSize:" + queue.size());
                            Thread.sleep(configHandler.getIntValue("smtp.mail.service.busy.sleep.time", 1000));
                            count = 0;
                        }
                    } else {
                        Thread.sleep(configHandler.getIntValue("smtp.mail.service.idle.sleep.time", 10000));
                    }
                } catch (Exception e) {
                    logger.error("Send Mail Task Error.", e);
                }
            }
        }

        private void sendMail(SlbMail mailData) throws JsonProcessingException {
            String data = ObjectJsonWriter.write(mailData);
            try {
                logger.info("[[SmtpMail=send]]Start Send Mail.MailData:" + data);
                HtmlEmail mail = new HtmlEmail();
                mail.setHostName(configHandler.getStringValue("smtp.mail.server.host", "mail.server.com"));
                mail.setSmtpPort(configHandler.getIntValue("smtp.mail.server.port", 25));
                mail.setCharset("UTF-8");
                //From
                mail.setFrom(mailData.getFrom() == null ? configHandler.getStringValue("slb.team.mail", "alias@domain.com")
                         : mailData.getFrom(), "SLB Team");
                //To
                mail.addTo(mailData.getRecipients().toArray(new String[mailData.getRecipients().size()]));
                //BCC
                if (mailData.getBcc() != null && mailData.getBcc().size() > 0) {
                    mail.addBcc(mailData.getBcc().toArray(new String[mailData.getBcc().size()]));
                }
                //CC
                if (mailData.getCc() != null && mailData.getCc().size() > 0) {
                    mail.addBcc(mailData.getCc().toArray(new String[mailData.getCc().size()]));
                }
                //Subject
                mail.setSubject(mailData.getSubject());
                //Inline Data
                if (mailData.getInlineData() != null && mailData.getInlineData().size() > 0) {
                    for (String key : mailData.getInlineData().keySet()) {
                        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(mailData.getInlineData().get(key), "application/octet-stream");
                        mail.embed(byteArrayDataSource, key, key);
                    }
                }
                //attach Data
                if (mailData.getAttachData() != null && mailData.getAttachData().size() > 0) {
                    for (String key : mailData.getAttachData().keySet()) {
                        String mime = mailData.getAttachContentType().get(key);
                        ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(mailData.getAttachData().get(key),
                                mime == null ? "application/octet-stream" : mime);
                        mail.attach(byteArrayDataSource, MimeUtility.decodeText(key), key);
                    }
                }

                mail.setHtmlMsg(mailData.getBody());
                mail.send();
                logger.info("[[SmtpMail=send]]Finished Send Mail.MailData:" + data);
            } catch (Exception e) {
                logger.error("[[SmtpMail=send]]Failed. Send Mail.MailData:" + data, e);
            }
        }

    }
}
