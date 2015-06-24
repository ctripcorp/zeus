package com.ctrip.zeus.service.model.handler.impl;

/**
 * Created by zhoumy on 2015/6/23.
 */
public class Tokenizer {
    private byte[] data;
    private int index;

    public Tokenizer(byte[] data) {
        this.data = data;
        index = 0;
    }

    public Token getStringToken(Token startToken) {
        byte[] debug = new byte[1024];
        int offset = index;
        int depth = startToken.compareTo(Token.MatchStart) == 0 ? 1 : 0;
        while (!endOfBytes(index)) {
            switch (data[index]) {
                case '\\': {
                    index++;
                    if (data[index] == '\"')
                        index++;
                    break;
                }
                case '\"': {
                    if (endOfBytes(index + 1))
                        return Token.Error;
                    for (int i = offset; i < index; i++) {
                        debug[i - offset] = data[i];
                    }
                    System.out.println(new String(debug));
                    return Token.StringContent;
                }
                case ';': {
                    if (depth > 0) {
                        index++;
                        break;
                    }
                    if (endOfBytes(index + 1)) {
                        for (int i = offset; i < index; i++) {
                            debug[i - offset] = data[i];
                        }
                        System.out.println(new String(debug));
                        return Token.StringContent;
                    }
                    if (data[index + 1] == '\"') {
                        for (int i = offset; i < index; i++) {
                            debug[i - offset] = data[i];
                        }
                        System.out.println(new String(debug));
                        return Token.StringContent;
                    }
                    index++;
                    break;
                }
                case ' ':
                    return Token.Error;
                default:
                    index++;
                    break;
            }
        }
        for (int i = offset; i < index; i++) {
            debug[i - offset] = data[i];
        }
        System.out.println(new String(debug));
        return Token.StringContent;

    }

    public Token getToken() {
        if (endOfBytes(index))
            return Token.Eof;
        switch (data[index]) {
            case '\"': {
                if (endOfBytes(++index))
                    return Token.Error;
                if (data[index] == ' ')
                    return Token.MatchEnd;
                return Token.MatchStart;
            }
            case ' ':
                index++;
                return Token.Delimiter;
            case ';':
                index++;
                return Token.RuleEnd;
            default:
                return Token.Error;

        }
    }

    private boolean endOfBytes(int index) {
        return index >= data.length;
    }

    public enum Token {
        MatchStart,
        MatchEnd,
        Delimiter,
        RuleEnd,
        StringContent,
        Eof,
        Error
    }
}
