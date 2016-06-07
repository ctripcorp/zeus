package com.ctrip.zeus.logstats.parser.state;

import java.util.List;

/**
 * Created by zhoumy on 2016/6/7.
 */
public class WrappedStringState implements LogStatsState<String> {
    private Transition transition = new WrappedStringTransition();

    @Override
    public String getOutput(StateContext ctxt) {
        transition.execute(ctxt);
        return null;
    }

    @Override
    public boolean shouldDeplay() {
        return false;
    }

    @Override
    public List<Transition> getDelayedTransition() {
        return null;
    }

    @Override
    public LogStatsStateMachine getSubMachine() {
        return null;
    }

    @Override
    public Transition getTranstition() {
        return transition;
    }

    private class WrappedStringTransition implements Transition {

        @Override
        public void execute(StateContext ctxt) {
            char[] matcher = new char[]{Character.MIN_VALUE, Character.MIN_VALUE};
            StringBuilder sb = new StringBuilder();
            char c;
            boolean _continue = false;
            boolean exit = false;
            char[] source = ctxt.getSource();
            for (int i = ctxt.getCurrentIndex(); i < source.length; i++) {
                c = source[i];
                switch (c) {
                    case '[': {
                        if (matcher[0] == Character.MIN_VALUE) {
                            matcher[0] = c;
                            break;
                        } // otherwise fall through
                    }
                    case ']': {
                        if (matcher[0] == '[') {
                            matcher[1] = ']';
                            exit = true;
                            break;
                        }
                    }
                    case ' ': {
                        if (matcher[0] == Character.MIN_VALUE) {
                            matcher[0] = c;
                            break;
                        } else {
                            if (matcher[0] == c) {
                                matcher[1] = c;
                                exit = true;
                                break;
                            }
                        }
                    }
                    case '\"': {
                        if (matcher[0] == Character.MIN_VALUE) {
                            matcher[0] = c;
                            break;
                        } else {
                            if (!_continue && matcher[0] == c) {
                                matcher[1] = c;
                                exit = true;
                                break;
                            }
                        }
                    }
                    case '\\':
                        _continue = !_continue;
                    default:
                        sb.append(c);
                }
                if (exit) {
                    ctxt.proceed(i - ctxt.getCurrentIndex() + 1);
                    ctxt.addResult(sb.toString());
                    System.out.println(sb.toString());
                    break;
                }
            }
        }
    }
}
