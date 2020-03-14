package com.ctrip.zeus.service.nginx.util;

import com.ctrip.zeus.exceptions.NginxProcessingException;
import com.ctrip.zeus.exceptions.NotFoundException;
import com.ctrip.zeus.model.model.DyUpstreamOpsData;
import com.ctrip.zeus.model.nginx.ConfFile;
import com.ctrip.zeus.model.nginx.NginxConfEntry;
import com.ctrip.zeus.service.query.GroupCriteriaQuery;
import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by zhoumy on 2016/3/16.
 */
@Component("upstreamConfPicker")
public class UpstreamConfPicker {
    @Resource
    private GroupCriteriaQuery groupCriteriaQuery;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String Identifier = "upstream";

    public DyUpstreamOpsData[] pickByGroupIds(NginxConfEntry entry, final Set<Long> groupIds) throws Exception {
        final List<DyUpstreamOpsData> result = new ArrayList<>();
        final Set<Long> selected = new HashSet<>();
        for (ConfFile cf : entry.getUpstreams().getFiles()) {
            parse(cf.getContent(), new UpstreamDirectiveDelegate() {
                @Override
                public void delegate(String upstreamName, String content) {
                    try {
                        Long groupId = Long.parseLong(upstreamName.substring(8));
                        if (groupIds.contains(groupId)) {
                            result.add(new DyUpstreamOpsData().setUpstreamName(upstreamName).setUpstreamCommands(content));
                            selected.add(groupId);
                        }
                    } catch (Exception ex) {
                        logger.warn("Upstream name might be invalid.", ex);
                    }
                }
            });
        }
        Set<Long> check = new HashSet<>(groupIds);
        check.removeAll(selected);
        if (!check.isEmpty()) {
            Set<Long> vgroups = null;
            try {
                vgroups = groupCriteriaQuery.queryAllVGroups();
            } catch (Exception e) {
                logger.error("Get VGroup Ids Failed.", e);
            }
            if (vgroups == null || !vgroups.containsAll(check)) {
                throw new NotFoundException("Cannot find upstream confs of groups " + Joiner.on(",").join(check) + ".");
            }
        }
        return result.toArray(new DyUpstreamOpsData[result.size()]);
    }

    public void parse(String upstreamFileEntry, UpstreamDirectiveDelegate delegate) throws NginxProcessingException {
        Stack<Character> grammarCheck = new Stack<>();
        int i = 0;
        String upstreamName = "";
        StringBuilder sb = new StringBuilder();
        while (i < upstreamFileEntry.length()) {
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
                            upstreamName = "";
                        } catch (Exception ex) {
                            logger.error("An unexpected error occurred when delegating upstream conf to picker.", ex);
                        }
                    } else {
                        sb.append(c);
                    }
                    break;
                }
                case '\t':
                case ' ': {
                    if (grammarCheck.empty()) {
                        String value = sb.toString();
                        if (Identifier.equals(value)) {
                            sb.setLength(0);
                            grammarCheck.push(' ');
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
                        sb.append(' ');
                    }
                    while (upstreamFileEntry.charAt(i + 1) == ' ' || upstreamFileEntry.charAt(i + 1) == '\t') {
                        i++;
                    }
                    break;
                }
                case '\r':
                    if (upstreamFileEntry.charAt(i + 1) == '\n') {
                        i++;
                    } else {
                        sb.append(c);
                    }
                    break;
                case '\n':
                    break;
                case '"': {
                    sb.append(c);
                    while ((c = upstreamFileEntry.charAt(++i)) != '"') {
                        sb.append(c);
                    }
                    sb.append(c);
                    break;
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