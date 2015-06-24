package com.ctrip.zeus.service.model.handler.impl;


/**
 * Created by zhoumy on 2015/6/23.
 */
public class RewriteParser {
    ParserState currentState;

    public boolean validate(byte[] data) {
        Tokenizer tokenizer = new Tokenizer(data);
        currentState = new ParserState();
        while (true) {
            Tokenizer.Token t = tokenizer.getToken();
            if (t.compareTo(Tokenizer.Token.Eof) == 0)
                return true;
            currentState.moveToNext();
            if (!currentState.matchState(t))
                return false;
            switch (t) {
                case MatchStart:
                    if (!handleContent(tokenizer))
                        return false;
                    break;
                case Delimiter:
                    if (!handleContent(tokenizer))
                        return false;
                    break;
                case Error:
                    return false;
            }
        }
    }

    private boolean handleContent(Tokenizer tokenizer) {
        Tokenizer.Token result = tokenizer.getStringToken(currentState.state);
        return currentState.moveToNext().matchState(result);
    }

    public class ParserState {
        Tokenizer.Token state;
        int depth;

        public ParserState() {
            state = Tokenizer.Token.Eof;
            depth = 0;
        }

        public ParserState moveToNext() {
            switch (state) {
                case Eof:
                    state = Tokenizer.Token.MatchStart;
                    break;
                case MatchStart:
                    depth++;
                    state = Tokenizer.Token.StringContent;
                    break;
                case MatchEnd:
                    depth--;
                    state = Tokenizer.Token.Delimiter;
                    break;
                case Delimiter:
                    state = Tokenizer.Token.StringContent;
                    break;
                case RuleEnd:
                    state = Tokenizer.Token.MatchStart;
                    break;
                case StringContent:
                    if (depth >= 1)
                        state = Tokenizer.Token.MatchEnd;
                    else
                        state = Tokenizer.Token.RuleEnd;
                    break;
                case Error:
                    break;
            }
            return this;
        }

        public boolean matchState(Tokenizer.Token token) {
            return currentState.state.compareTo(token) == 0;
        }
    }
}
