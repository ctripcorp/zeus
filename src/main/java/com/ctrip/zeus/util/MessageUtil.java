package com.ctrip.zeus.util;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;
import com.ctrip.zeus.queue.entity.GroupData;
import com.ctrip.zeus.queue.entity.SlbData;
import com.ctrip.zeus.queue.entity.SlbMessageData;
import com.ctrip.zeus.queue.entity.VsData;
import com.ctrip.zeus.queue.transform.DefaultJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by fanqq on 2016/10/13.
 */
public class MessageUtil {
    static Logger logger = LoggerFactory.getLogger(MessageUtil.class);

    public static String getMessageData(HttpServletRequest request, Group[] groups, VirtualServer[] vses, Slb[] slbs, String[] ips, boolean success) {
        SlbMessageData res = new SlbMessageData();
        res.setQuery(request.getQueryString())
                .setUri(request.getRequestURI())
                .setSuccess(success)
                .setClientIp(getClientIP(request));
        if (groups != null && groups.length > 0) {
            for (Group group : groups) {
                res.addGroupData(new GroupData().setId(group.getId()).setName(group.getName()).setVersion(group.getVersion()));
            }
        }
        if (slbs != null && slbs.length > 0) {
            for (Slb slb : slbs) {
                res.addSlbData(new SlbData().setId(slb.getId()).setName(slb.getName()).setVersion(slb.getVersion()));
            }
        }
        if (vses != null && vses.length > 0) {
            for (VirtualServer vs : vses) {
                res.addVsData(new VsData().setId(vs.getId()).setName(vs.getName()).setVersion(vs.getVersion()));
            }
        }
        if (ips != null && ips.length > 0) {
            for (String ip : ips) {
                res.addIp(ip);
            }
        }
        return String.format(SlbMessageData.JSON, res);
    }

    public static SlbMessageData parserSlbMessageData(String res) {
        try {
            if (res == null) return null;
            return DefaultJsonParser.parse(SlbMessageData.class, res);
        } catch (IOException e) {
            logger.warn("Parser Slb Message Data Failed. Message:" + res, e);
            return null;
        }
    }

    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
