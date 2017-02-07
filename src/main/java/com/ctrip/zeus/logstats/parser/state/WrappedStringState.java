package com.ctrip.zeus.logstats.parser.state;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class WrappedStringState implements LogStatsState {
    private final String name;
    private final char startSymbol;
    private final char endSymbol;
    private final Action action;

    private LogStatsState next;

    public WrappedStringState(char startSymbol, char endSymbol, String name) {
        this.startSymbol = startSymbol;
        this.endSymbol = endSymbol;
        this.name = name;
        this.action = new WrappedStringAction();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LogStatsStateMachine getSubMachine() {
        return null;
    }

    @Override
    public Action getAction() {
        return action;
    }

    @Override
    public void setNext(LogStatsState next) {
        this.next = next;
    }

    @Override
    public LogStatsState getNext() {
        return next;
    }

    @Override
    public boolean runSubMachine() {
        return false;
    }

    private class WrappedStringAction implements Action {

        @Override
        public void execute(StateMachineContext ctxt) {
            char[] matcher = new char[]{Character.MIN_VALUE, Character.MIN_VALUE};
            StringBuilder sb = new StringBuilder();
            char c;
            boolean _escaping = false;
            char[] source = ctxt.getSource();
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                c = source[i];
                if (c == startSymbol) {
                    if (!_escaping && matcher[0] == Character.MIN_VALUE) {
                        if (sb.length() == 0) {
                            matcher[0] = c;
                            continue;
                        } else {
                            ctxt.setState(StateMachineContext.ContextState.FAILURE);
                            return;
                        }
                    }
                }
                if (c == endSymbol) {
                    if (!_escaping && matcher[0] == startSymbol) {
                        matcher[1] = c;
                        ctxt.proceed(i - ctxt.getCurrentIndex() + 1);
                        ctxt.addResult(name, sb.toString());
                        return;
                    }
                }

                if (_escaping) {
                    sb.append('\\' + c);
                    _escaping = !_escaping;
                    continue;
                }

                if (c == '\\') {
                    _escaping = !_escaping;
                } else {
                    sb.append(c);
                }
            }

            if (matcher[0] == Character.MIN_VALUE || matcher[0] == Character.MIN_VALUE) {
                ctxt.setState(StateMachineContext.ContextState.FAILURE);
                return;
            }
            ctxt.proceed(source.length - ctxt.getCurrentIndex() + 1);
            ctxt.addResult(name, sb.toString());
        }
    }
}
