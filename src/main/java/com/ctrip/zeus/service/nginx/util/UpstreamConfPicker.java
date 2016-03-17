package com.ctrip.zeus.service.nginx.util;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.model.entity.DyUpstreamOpsData;
import com.ctrip.zeus.nginx.entity.VsConfData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by zhoumy on 2016/3/16.
 */
@Component("upstreamConfPicker")
public class UpstreamConfPicker {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String Identifier = "upstream";

    public DyUpstreamOpsData[] pickByGroupIds(Map<Long, VsConfData> vsConf, final Set<Long> groupIds) throws NginxProcessingException {
        final List<DyUpstreamOpsData> result = new ArrayList<>();
        for (VsConfData data : vsConf.values()) {
            parse(data.getUpstreamConf(), new UpstreamDirectiveDelegate() {
                @Override
                public void delegate(String upstreamName, String content) {
                    try {
                        if (groupIds.contains(Long.parseLong(upstreamName.substring(8)))) {
                            result.add(new DyUpstreamOpsData().setUpstreamName(upstreamName).setUpstreamCommands(content));
                        }
                    } catch (Exception ex) {
                        logger.warn("Upstream name might be invalid.", ex);
                    }
                }
            });
        }
        return result.toArray(new DyUpstreamOpsData[result.size()]);
    }

    public void parse(String upstreamFileEntry, UpstreamDirectiveDelegate delegate) throws NginxProcessingException {
        Stack<Character> grammarCheck = new Stack<>();

        int i = 0;
        String upstreamName = "";
        while (i < upstreamFileEntry.length()) {
            StringBuilder sb = new StringBuilder();
            char c;
            switch (c = upstreamFileEntry.charAt(i)) {
                case '{': {
                    if (grammarCheck.isEmpty()) grammarCheck.push(c);
                    else sb.append(c);
                    break;
                }
                case '}': {
                    if (grammarCheck.empty()) {
                        throw new NginxProcessingException("Invalid vs conf format, '}' is found at unexpected position.");
                    }
                    if (grammarCheck.peek().charValue() == '{') {
                        grammarCheck.pop();
                        String upstreamConfig = sb.toString();
                        sb.setLength(0);
                        try {
                            delegate.delegate(upstreamName, upstreamConfig);
                        } catch (Exception ex) {
                            logger.error("An unexpected error occurred when delegating upstream conf to picker.", ex);
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                case ' ': {
                    if (grammarCheck.empty()) {
                        String value = sb.toString();
                        if (Identifier.equals(value)) {
                            sb.setLength(0);
                            grammarCheck.push(c);
                            while (upstreamFileEntry.charAt(i + 1) == ' ') {
                                i++;
                            }
                            break;
                        } else {
                            throw new NginxProcessingException("Invalid vs conf format, ' ' is found at unexpected position.");
                        }
                    }
                    if (grammarCheck.peek().charValue() == ' ') {
                        grammarCheck.pop();
                        upstreamName = sb.toString();
                        sb.setLength(0);
                    } else {
                        grammarCheck.push(c);
                    }
                    while (upstreamFileEntry.charAt(i + 1) == ' ') {
                        i++;
                    }
                }
                default: {
                    sb.append(c);
                    break;
                }
            }
            i++;
        }
    }

    interface UpstreamDirectiveDelegate {
        void delegate(String upstreamName, String content);
    }
}