package com.ctrip.zeus.logstats.common;

import com.ctrip.zeus.logstats.parser.state.*;
import com.ctrip.zeus.logstats.parser.state.extended.RequestUriState;
import com.ctrip.zeus.logstats.parser.state.extended.UpstreamState;
import com.ctrip.zeus.logstats.parser.state.extended.XForwardState;

import java.util.*;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class AccessLogFormat {
    private String format;
    private final Map<String, LogStatsState> logStateRegitry = new HashMap<>();
    private LogStatsStateMachine stateMachine;
    private LogStatsState initState;

    public AccessLogFormat() {
        registerPatternForKey("http_x_forwarded_for", new XForwardState("http_x_forwarded_for"));
        registerPatternForKey("request_uri", new RequestUriState("request_uri"));
        registerPatternForKey("upstream_response_time", new UpstreamState("upstream_response_time"));
        registerPatternForKey("upstream_addr", new UpstreamState("upstream_addr"));
        registerPatternForKey("upstream_status", new UpstreamState("upstream_status"));
    }


    public String[] getKeys() {
        return null;
    }


    public AccessLogFormat setFormat(String format) {
        this.format = format;
        return this;
    }


    public AccessLogFormat registerPatternForKey(String key, LogStatsState state) {
        logStateRegitry.put(key, state);
        return this;
    }

    public LogStatsStateMachine getStateMachine() {
        return stateMachine;
    }

    public AccessLogFormat generate() {
        parsePattern();
        stateMachine = new AccessLogStateMachine(initState);
        return this;
    }

    protected void parsePattern() {
        LogStatsState state = null;
        char[] formatChars = format.toCharArray();
        Stack<String> operands = new Stack<>();
        for (int i = 0; i < formatChars.length; i++) {
            // whitespace or predefined
            switch (formatChars[i]) {
                case ' ':
                    if (operands.size() != 1 && operands.size() != 3) {
                        throw new RuntimeException("not implemented");
                    } else {
                        LogStatsState s = null;
                        if (operands.size() == 1) {
                            String name = operands.pop();
                            s = logStateRegitry.get(name);
                            if (s == null) {
                                s = new StringState(name);
                            }
                        }
                        if (operands.size() == 3) {
                            char end = operands.pop().charAt(0);
                            String name = operands.pop();
                            char start = operands.pop().charAt(0);
                            s = new WrappedStringState(start, end, name);
                        }
                        if (s == null) throw new RuntimeException();
                        if (state == null) {
                            state = s;
                            initState = state;
                        } else {
                            state.setNext(s);
                            state = s;
                        }
                    }
                    break;
                case '"':
                    operands.push("\"");
                    break;
                case '[':
                    operands.push("[");
                    break;
                case ']':
                    operands.push("]");
                    break;
                default:
                    throw new RuntimeException("not implemented");
            }

            if (formatChars[i] == '$') {
                int start = i;
                // word characters
                while (++i < formatChars.length && (Character.isLetterOrDigit(formatChars[i]) || formatChars[i] == '_')) {
                }
                String k = i - start == 0 ? "key" + i : new String(formatChars, start + 1, i - start - 1);
                operands.push(k);
                operands.push("$");
                i++;
            }
            if (i >= formatChars.length)
                break;
        }
    }
}
