package com.ctrip.zeus.service.build.util;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResolverUtils {
    public static final String RESOLVER_SPACE = "#PlaceForResolver";
    private static final String RESOLVER_PATH = "/etc/resolv.conf";

    private static Logger logger = LoggerFactory.getLogger(ResolverUtils.class);

    public static List<String> getLocalResolvers() throws IOException {
        BufferedReader bufferedReader = null;
        try {
            List<String> res = new ArrayList<>();
            FileReader fileReader = new FileReader(RESOLVER_PATH);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            Pattern pattern = Pattern.compile("^( *)nameserver( )+(.*)$");
            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String ip = matcher.group(3).trim();
                    if (InetAddressValidator.getInstance().isValid(ip)) {
                        res.add(ip);
                    }
                }
            }
            if (res.size() > 0) {
                return res;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Get Local Resolver Failed.", e);
            return null;
        } finally {
            if (bufferedReader != null) bufferedReader.close();
        }
    }
}
