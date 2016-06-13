package com.ctrip.zeus.logstats.common;

import com.ctrip.zeus.logstats.parser.state.*;
import com.ctrip.zeus.logstats.parser.state.extended.RequestUriState;
import com.ctrip.zeus.logstats.parser.state.extended.UpstreamState;
import com.ctrip.zeus.logstats.parser.state.extended.XForwardState;

import java.util.*;

/**
 * Created by zhoumy on 2016/6/6.
 */
public class AccessLogStateMachineFormat implements LineFormat<LogStatsStateMachine, LogStatsState> {
    private String format;
    private final Map<String, LogStatsState> logStateRegitry = new HashMap<>();
    private String[] keys;
    private LogStatsStateMachine stateMachine;
    private LogStatsState firstState;
    private LogStatsState lastState;

    public AccessLogStateMachineFormat() {
    }

    public AccessLogStateMachineFormat(String format) {
        setFormat(format);
        registerComponentForKey("http_x_forwarded_for", new XForwardState("http_x_forwarded_for"));
        registerComponentForKey("request_uri", new RequestUriState("request_uri"));
        registerComponentForKey("upstream_response_time", new UpstreamState("upstream_response_time"));
        registerComponentForKey("upstream_addr", new UpstreamState("upstream_addr"));
        registerComponentForKey("upstream_status", new UpstreamState("upstream_status"));
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public LogStatsStateMachine getEngine() {
        return stateMachine;
    }

    public String[] getKeys() {
        return keys;
    }

    public AccessLogStateMachineFormat setFormat(String format) {
        this.format = format;
        return this;
    }

    @Override
    public LineFormat registerComponentForKey(String key, LogStatsState component) {
        logStateRegitry.put(key, component);
        return this;
    }

    public AccessLogStateMachineFormat generate() {
        parsePattern();
        stateMachine = new AccessLogStateMachine(firstState);
        return this;
    }

    protected void parsePattern() {
        lastState = firstState = null;
        char[] formatChars = format.toCharArray();
        Stack<String> operands = new Stack<>();
        for (int i = 0; i < formatChars.length; i++) {
            if (formatChars[i] == '$') {
                int start = i;
                // word characters
                while (++i < formatChars.length && (Character.isLetterOrDigit(formatChars[i]) || formatChars[i] == '_')) {
                }
                String k = i - start == 0 ? "key" + i : new String(formatChars, start + 1, i - start - 1);
                operands.push(k);
            }
            if (i >= formatChars.length) break;

            // whitespace or predefined
            switch (formatChars[i]) {
                case ' ':
                    appendState(operands);
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
                    System.out.println(formatChars[i]);
                    throw new RuntimeException("not implemented");
            }
        }
        appendState(operands);

        LogStatsState state = firstState;
        if (state != null) {
            List<String> keyList = new ArrayList<>();
            keyList.add(state.getName());
            while ((state = state.getNext()) != null) {
                keyList.add(state.getName());
            }
            keys = keyList.toArray(new String[keyList.size()]);
        }
    }

    private void appendState(Stack<String> operands) {
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
            if (lastState == null) {
                lastState = s;
                firstState = lastState;
            } else {
                lastState.setNext(s);
                lastState = s;
            }
        }
    }
}
