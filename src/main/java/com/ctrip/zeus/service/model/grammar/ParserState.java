package com.ctrip.zeus.service.model.grammar;

/**
 * Created by zhoumy on 2015/6/29.
 */
public class ParserState {
    protected Tokenizer.Token state;
    private int depth;

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

    public void setState(Tokenizer.Token state) {
        this.state = state;
    }

    public boolean matchState(Tokenizer.Token token) {
        return state.compareTo(token) == 0;
    }

    @Override
    public String toString() {
        return state.toString();
    }
}
