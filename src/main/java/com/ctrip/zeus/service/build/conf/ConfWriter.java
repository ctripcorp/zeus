package com.ctrip.zeus.service.build.conf;

import com.ctrip.zeus.exceptions.ValidationException;

/**
 * Created by zhoumy on 2016/4/12.
 */
public class ConfWriter {
    private static final String ClosingBrace = "}";
    private static final String IndentationLevel = "    ";
    private final StringBuilder stringBuilder;
    private int depth = 0;
    private boolean pretty = false;

    public ConfWriter(int size, boolean pretty) {
        stringBuilder = new StringBuilder();
        this.pretty = pretty;
    }

    public ConfWriter(int size) {
        this(size, false);
    }

    public ConfWriter() {
        this(16, false);
    }

    public ConfWriter writeLine(String line) {
        indent();
        for (char c : line.toCharArray()) {
            switch (c) {
                case '\n': {
                    stringBuilder.append(c);
                    indent();
                    break;
                }
                default:
                    stringBuilder.append(c);
                    break;
            }
        }
        stringBuilder.append("\n");
        return this;
    }

    public ConfWriter writeCommand(String key, String value) {
        indent();
        stringBuilder.append(key == null ? "" : key).append(" ");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\n': {
                    stringBuilder.append(c);
                    indent();
                    break;
                }
                default:
                    stringBuilder.append(c);
                    break;
            }
        }
        stringBuilder.append(";\n");
        return this;
    }

    public ConfWriter writeHttpStart() {
        writeLine("http {");
        depth++;
        return this;
    }

    public ConfWriter writeHttpEnd() {
        depth--;
        writeLine(ClosingBrace);
        return this;
    }

    public ConfWriter writeEventsStart() {
        writeLine("events {");
        depth++;
        return this;
    }

    public ConfWriter writeEventsEnd() {
        depth--;
        indent();
        writeLine(ClosingBrace);
        return this;
    }

    public ConfWriter writeServerStart() {
        writeLine("server {");
        depth++;
        return this;
    }

    public ConfWriter writeServerEnd() {
        depth--;
        writeLine(ClosingBrace);
        return this;
    }

    public ConfWriter writeLocationStart(String path) {
        indent();
        stringBuilder.append("location ").append(path).append(" {\n");
        depth++;
        return this;
    }

    public ConfWriter writeLocationEnd() {
        depth--;
        writeLine(ClosingBrace);
        return this;
    }

    public ConfWriter writeUpstreamStart(String upstreamName) {
        indent();
        stringBuilder.append("upstream ").append(upstreamName).append(" {\n");
        depth++;
        return this;
    }

    public ConfWriter writeUpstreamEnd() {
        depth--;
        writeLine(ClosingBrace);
        return this;
    }

    public ConfWriter writeUpstreamServer(String ip, int port, int weight, int maxFails, int failTimeout, boolean down) {
        indent();
        stringBuilder.append("server ").append(ip + ":" + port)
                .append(" weight=").append(weight)
                .append(" max_fails=").append(maxFails)
                .append(" fail_timeout=").append(failTimeout)
                .append(down ? " down" : "").append(";\n");
        return this;
    }

    public ConfWriter writeIfStart(String condition) {
        indent();
        stringBuilder.append("if (").append(condition).append(") {\n");
        depth++;
        return this;
    }

    public ConfWriter writeIfEnd() {
        depth--;
        writeLine(ClosingBrace);
        return this;
    }

    public ConfWriter write(String value) {
        stringBuilder.append(value);
        return this;
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    public String getValue() throws ValidationException {
        if (depth != 0)
            throw new ValidationException("Grammar check fails during conf writing. Depth is expected to be 0, but was " + depth);
        return stringBuilder.toString();
    }

    private void indent() {
        if (pretty) {
            for (int i = 0; i < depth; i++) {
                stringBuilder.append(IndentationLevel);
            }
        }
    }
}

