package com.ctrip.zeus.util;


import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Discription
 **/
public class CertUtil {

    private static CertificateFactory CERTIFICATE_FACTORY = null;

    private final static String DOMAIN_TAG_PREFIX = "cert_domain_";

    public static final String CERT_ELEM_TYPE = "cert";

    static {
        try {
            CERTIFICATE_FACTORY = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            // ignore
        }
    }

    /*
     * @Description generate a CertWrapper instance for ssl.crt inputstream
     * @return: a CertWrapper instance or throws CertificateException on parsing crt file error
     **/
    public static CertWrapper getCertWrapper(InputStream certInputStream) throws CertificateException {
        return new CertWrapper(certInputStream);
    }

    public static class CertWrapper {

        private final X509Certificate delegate;

        public CertWrapper(InputStream crtFile) throws CertificateException {
            delegate = (X509Certificate) CERTIFICATE_FACTORY.generateCertificate(crtFile);
        }

        public Date getIssueTime() {
            return delegate.getNotBefore();
        }

        public Date getExpireTime() {
            return delegate.getNotAfter();
        }

        public List<String> getDomains() {
            return new ArrayList<>();
        }
    }

    public static String buildTagOf(String domain) {
        if (!Strings.isNullOrEmpty(domain)) {
            return DOMAIN_TAG_PREFIX + standardizeDomain(domain);
        }

        return null;
    }

    public static String buildTagOf(List<String> domains) {
        if (domains != null) {
            String domain = Joiner.on("|").join(domains);
            return buildTagOf(domain);
        }

        return null;
    }

    public static boolean isCertDomainTag(String tagName) {
        return tagName != null && tagName.startsWith(DOMAIN_TAG_PREFIX);
    }

    public static String parseDomainOutOfTag(String tagName) {
        if (tagName != null && tagName.startsWith(DOMAIN_TAG_PREFIX)) {
            return tagName.substring(DOMAIN_TAG_PREFIX.length());
        }
        return null;
    }

    public static String standardizeDomain(String domain) {
        if (Strings.isNullOrEmpty(domain)) {
            return null;
        }

        String[] domains = domain.split("\\|");
        for (int i = 0; i != domains.length; i++) {
            domains[i] = domains[i].toLowerCase();
        }
        Arrays.sort(domains);

        return Joiner.on("|").join(domains);
    }

    public static String[] splitDomain(String domain) {
        if (domain == null) {
            return new String[0];
        }

        return domain.split("\\|");
    }
}
