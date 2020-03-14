package com.ctrip.zeus.util;

import com.ctrip.zeus.auth.entity.User;
import com.ctrip.zeus.auth.util.AuthUtil;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by fanqq on 2016/7/25.
 */
public class UserUtils {
    public static String getUserName(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        Assertion assertion = session != null ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : null;
        String userName;
        if (assertion != null && assertion.getPrincipal() != null) {
            userName = assertion.getPrincipal().getName();
        } else {
            userName = request.getAttribute(AuthUtil.AUTH_USER) == null ? null : request.getAttribute(AuthUtil.AUTH_USER).toString();
        }
        return userName;
    }

    public static User getUser(HttpServletRequest request) {
        User user = new User();
        final HttpSession session = request.getSession(false);
        Assertion assertion = session != null ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : null;
        String userName = null;
        if (assertion != null && assertion.getPrincipal().getAttributes().size() > 0) {
            user.setEmail(assertion.getPrincipal().getAttributes().get("mail").toString());
            user.setUserName(assertion.getPrincipal().getAttributes().get("name").toString());
            user.setBu(assertion.getPrincipal().getAttributes().get("department").toString());
            user.setChineseName(assertion.getPrincipal().getAttributes().get("displayName").toString());
        } else {
            userName = request.getAttribute(AuthUtil.AUTH_USER) == null ? null : request.getAttribute(AuthUtil.AUTH_USER).toString();
            String email = request.getAttribute(AuthUtil.AUTH_USER_EMAIL) == null ? null : request.getAttribute(AuthUtil.AUTH_USER_EMAIL).toString();
            String bu = request.getAttribute(AuthUtil.AUTH_USER_BU) == null ? null : request.getAttribute(AuthUtil.AUTH_USER_BU).toString();
            String cname = request.getAttribute(AuthUtil.AUTH_USER_CNAME) == null ? null : request.getAttribute(AuthUtil.AUTH_USER_CNAME).toString();
            user.setUserName(userName);
            if (email != null) {
                user.setEmail(email);
            }
            if (bu != null) {
                user.setBu(bu);
            }
            if (cname != null) {
                user.setChineseName(cname);
            }
        }
        if (user.getUserName() != null) {
            return user;
        } else {
            return null;
        }
    }

    public static String getEmployee(HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        Assertion assertion = session != null ? (Assertion) session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : null;
        if (assertion != null && assertion.getPrincipal().getAttributes().size() > 0) {
            return assertion.getPrincipal().getAttributes().get("employee").toString();
        }
        return null;
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
}
