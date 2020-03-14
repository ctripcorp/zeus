package com.ctrip.zeus.util;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.model.*;
import com.ctrip.zeus.model.queue.*;
import com.ctrip.zeus.support.ObjectJsonParser;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

/**
 * Created by fanqq on 2016/10/13.
 */
public class MessageUtil {
    private static Logger logger = LoggerFactory.getLogger(MessageUtil.class);

    public static void validateDescriptionInQuery(HttpServletRequest request) throws ValidationException {
        String query = request.getQueryString();
        if (Strings.isNullOrEmpty(getDescriptionFromQuery(query))) {
            throw new ValidationException("Please Add Description In Query String.Example: &description=reason:user:extraMessage");
        }
    }

    public static String getMessageData(HttpServletRequest request, Group[] groups, TrafficPolicy[] policies, Dr[] drs, VirtualServer[] vses, Slb[] slbs, String[] ips, boolean success, String uri, String out) {
        SlbMessageData res = new SlbMessageData();
        String query = request.getQueryString();
        String description = getDescriptionFromQuery(query);
        String outMsg = out;
        if (out != null && out.length() > 256) {
            outMsg = out.substring(0, 256);
        }
        res.setQuery(query)
                .setDescription(description)
                .setUri(request.getRequestURI().replaceAll("[/]+$", ""))
                .setSuccess(success)
                .setOutMessage(outMsg)
                .setUser(UserUtils.getUserName(request))
                .setClientIp(getClientIP(request));
        if (uri != null) {
            res.setUri(uri);
        }
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
        if (policies != null && policies.length > 0) {
            for (TrafficPolicy policy : policies) {
                res.addPolicyData(new PolicyData().setId(policy.getId()).setName(policy.getName()).setVersion(policy.getVersion()));
            }
        }
        if (drs != null && drs.length > 0) {
            for (Dr dr : drs) {
                res.addDrData(new DrData().setId(dr.getId()).setName(dr.getName()).setVersion(dr.getVersion()));
            }
        }
        try {
            return ObjectJsonWriter.write(res);
        } catch (Exception e) {
            logger.warn("Write Message Data Fail." + res.toString(), e);
            return null;
        }
    }

    public static String getMessageData(HttpServletRequest request, Group[] groups, TrafficPolicy[] policies, VirtualServer[] vses, Slb[] slbs, String[] ips, boolean success) {
        return getMessageData(request, groups, policies, null, vses, slbs, ips, success, null, null);
    }

    public static String getMessageData(HttpServletRequest request, Group[] groups, TrafficPolicy[] policies, VirtualServer[] vses, Slb[] slbs, String[] ips, String message, boolean success) {
        return getMessageData(request, groups, policies, null, vses, slbs, ips, success, null, message);
    }

    public static String getMessageData(HttpServletRequest request, Group[] groups, TrafficPolicy[] policies, Dr[] drs, VirtualServer[] vses, Slb[] slbs, String[] ips, boolean success) {
        return getMessageData(request, groups, policies, drs, vses, slbs, ips, success, null, null);
    }

    public static String getMessageData(HttpServletRequest request, Group[] groups, TrafficPolicy[] policies, VirtualServer[] vses, Slb[] slbs, String[] ips, String message, String uri, boolean success) {
        return getMessageData(request, groups, policies, null, vses, slbs, ips, success, uri, message);
    }


    public static String getDescriptionFromQuery(String query) {
        if (query == null) {
            return null;
        }
        String[] qs = query.split("&");
        String description = null;
        for (String tmp : qs) {
            if (tmp.startsWith("description=")) {
                String[] l = tmp.split("=");
                if (l.length == 2 && l[1] != null) {
                    try {
                        description = URLDecoder.decode(l[1], "utf-8");
                    } catch (Exception e) {
                        description = l[1];
                        logger.warn("Get Description Failed. Description:" + l[1]);
                    }

                }
            }
        }
        if (description != null && description.length() > 512) {
            description = description.substring(0, 512);
        }
        return description;
    }

    public static SlbMessageData parserSlbMessageData(String res) {
        try {
            if (res == null) return null;
            return ObjectJsonParser.parse(res, SlbMessageData.class);
        } catch (Exception e) {
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
        } else {
            ip = ip.split(",")[0];
        }
        return ip;
    }

    public static SlbMessageDataBuilder getMessageBuilder(HttpServletRequest request, boolean success) {
        return new MessageUtil.SlbMessageDataBuilder(request, success);
    }

    public static SlbMessageDataBuilder getMessageBuilder(String user, String uri, String ip, String query, String description, boolean success) {
        return new MessageUtil.SlbMessageDataBuilder(user, uri, ip, query, description, success);
    }

    public static SlbMessageDataBuilder getMessageBuilder(String user, String uri, String description, boolean success) {
        return new MessageUtil.SlbMessageDataBuilder(user, uri, null, null, description, success);
    }

    static public class SlbMessageDataBuilder {
        private SlbMessageData res = new SlbMessageData();

        SlbMessageDataBuilder(HttpServletRequest request, boolean success) {
            String query = request.getQueryString();
            String description = getDescriptionFromQuery(query);
            res.setQuery(query)
                    .setDescription(description)
                    .setUri(request.getRequestURI().replaceAll("[/]+$", ""))
                    .setSuccess(success)
                    .setUser(UserUtils.getUserName(request))
                    .setClientIp(getClientIP(request));
        }

        SlbMessageDataBuilder(String user, String uri, String ip, String query, String description, boolean success) {
            res.setQuery(query)
                    .setDescription(description)
                    .setUri(uri)
                    .setSuccess(success)
                    .setUser(user)
                    .setClientIp(ip);
        }

        public SlbMessageDataBuilder bindGroups(Group[] groups) {
            if (groups != null && groups.length > 0) {
                for (Group group : groups) {
                    res.addGroupData(new GroupData().setId(group.getId()).setName(group.getName()).setVersion(group.getVersion()));
                }
            }
            return this;
        }

        public SlbMessageDataBuilder bindType(String type) {
            if (type != null) {
                res.setType(type);
            }
            return this;
        }

        public SlbMessageDataBuilder bindCID(String cid) {
            if (cid != null) {
                if (res.getCertData() == null) {
                    res.setCertData(new CertData());
                }
                res.getCertData().setCid(cid);
            }
            return this;
        }

        public SlbMessageDataBuilder bindCertId(Long certId) {
            if (certId != null) {
                if (res.getCertData() == null) {
                    res.setCertData(new CertData());
                }
                res.getCertData().setCertId(certId);
            }
            return this;
        }

        public SlbMessageDataBuilder bindCertDomain(String domain) {
            if (domain != null) {
                if (res.getCertData() == null) {
                    res.setCertData(new CertData());
                }
                res.getCertData().setDomain(domain);
            }
            return this;
        }

        public SlbMessageDataBuilder bindPolicies(TrafficPolicy[] policies) {
            if (policies != null && policies.length > 0) {
                for (TrafficPolicy policy : policies) {
                    res.addPolicyData(new PolicyData().setId(policy.getId()).setName(policy.getName()).setVersion(policy.getVersion()));
                }
            }
            return this;
        }

        public SlbMessageDataBuilder bindVses(VirtualServer[] vses) {
            if (vses != null && vses.length > 0) {
                for (VirtualServer vs : vses) {
                    res.addVsData(new VsData().setId(vs.getId()).setName(vs.getName()).setVersion(vs.getVersion()));
                }
            }
            return this;
        }

        public SlbMessageDataBuilder bindDrs(Dr[] drs) {
            if (drs != null && drs.length > 0) {
                for (Dr dr : drs) {
                    res.addDrData(new DrData().setId(dr.getId()).setName(dr.getName()).setVersion(dr.getVersion()));
                }
            }
            return this;
        }

        public SlbMessageDataBuilder bindSlbs(Slb[] slbs) {
            if (slbs != null && slbs.length > 0) {
                for (Slb slb : slbs) {
                    res.addSlbData(new SlbData().setId(slb.getId()).setName(slb.getName()).setVersion(slb.getVersion()));
                }
            }
            return this;
        }

        public SlbMessageDataBuilder bindIps(String[] ips) {
            if (ips != null && ips.length > 0) {
                for (String ip : ips) {
                    res.addIp(ip);
                }
            }
            return this;
        }

        public SlbMessageDataBuilder bindUri(String uri) {
            if (uri != null) {
                uri = uri.replaceAll("[/]+$", "");
                res.setUri(uri);
            }
            return this;
        }

        public SlbMessageDataBuilder bindOutMessage(String out) {
            if (out != null) {
                res.setOutMessage(out);
            }
            return this;
        }

        public SlbMessageDataBuilder flow(String name, Long id) {
            res.setFlowData(new FlowData().setId(id).setName(name));
            return this;
        }

        public SlbMessageDataBuilder bindRules(Rule[] rules) {
            if (rules != null && rules.length > 0) {
                for (Rule rule : rules) {
                    res.addRuleData(new RuleData().setRuleType(rule.getRuleType()).setId(rule.getId()));
                }
            }
            return this;
        }


        public String build() {
            try {
                return ObjectJsonWriter.write(res);
            } catch (Exception e) {
                logger.warn("Write Message Data Fail." + res.toString(), e);
                return null;
            }
        }
    }
}
