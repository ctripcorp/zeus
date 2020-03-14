package com.ctrip.zeus.startup.impl;

import com.ctrip.zeus.service.file.SessionTicketService;
import com.ctrip.zeus.startup.PreCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("sessionTicketPreCheck")
public class SessionTicketPreCheck implements PreCheck {
    @Resource
    private SessionTicketService sessionTicketService;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean ready() {
        try {
            sessionTicketService.sessionTicketFileStartInit();
            return true;
        } catch (Exception e) {
            logger.error("Initialize Session Ticket File failed.", e);
            return false;
        }
    }
}
