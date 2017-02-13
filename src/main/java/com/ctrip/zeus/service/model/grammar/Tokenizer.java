package com.ctrip.zeus.service.model.grammar;

import com.ctrip.zeus.util.BytesUtils;

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

    public String getStringToken(ParserState currentState) {
        Token startToken = currentState.state;
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
                    if (endOfBytes(index + 1)) {
                        currentState.setState(Token.Error);
                        return "";
                    }
                    currentState.moveToNext();
                    return BytesUtils.toString(data, offset, index - offset);
                }
                case ';': {
                    if (depth > 0) {
                        index++;
                        break;
                    }
                    if (endOfBytes(index + 1)) {
                        currentState.moveToNext();
                        return BytesUtils.toString(data, offset, index - offset);
                    }
                    if (data[index + 1] == '\"') {
                        currentState.moveToNext();
                        return BytesUtils.toString(data, offset, index - offset);
                    }
                    index++;
                    break;
                }
                case ' ':
                    currentState.setState(Token.Error);
                    return "";
                default:
                    index++;
                    break;
            }
        }
        currentState.moveToNext();
        return BytesUtils.toString(data, offset, index - offset);
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
        MatchStart("Start \""),
        MatchEnd("End \""),
        Delimiter(" "),
        RuleEnd(";"),
        StringContent("String"),
        Eof("EOF"),
        Error("Error");

        private final String name;

        private Token(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
