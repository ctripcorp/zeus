package com.ctrip.zeus.service.model.grammar;


import java.util.List;

/**
 * Created by zhoumy on 2015/6/23.
 */
public class RewriteParseHandler {
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
                    handleContent(tokenizer);
                    if (!currentState.matchState(Tokenizer.Token.StringContent))
                        return false;
                    break;
                case Delimiter:
                    handleContent(tokenizer);
                    if (!currentState.matchState(Tokenizer.Token.StringContent))
                        return false;
                    break;
                case Error:
                    return false;
            }
        }
    }

    public void handleContent(byte[] data, List<String> output) throws ParseException {
        Tokenizer tokenizer = new Tokenizer(data);
        currentState = new ParserState();
        while (true) {
            Tokenizer.Token t = tokenizer.getToken();
            if (t.compareTo(Tokenizer.Token.Eof) == 0)
                return;
            currentState.moveToNext();
            if (!currentState.matchState(t))
                throw new ParseException("Expect " + currentState.toString() + ", but was " + t.toString() + ".");
            switch (t) {
                case MatchStart:
                    String content = handleContent(tokenizer);
                    if (currentState.matchState(Tokenizer.Token.StringContent))
                        output.add(content);
                    else
                        throw new ParseException("Cannot handle origin string pattern.");
                    break;
                case Delimiter:
                    String result = handleContent(tokenizer);
                    if (currentState.matchState(Tokenizer.Token.StringContent))
                        output.add(result);
                    else
                        throw new ParseException("Cannot handle rewrite string pattern.");
                    break;
                case Error:
                    throw new ParseException("Invalid value.");
            }
        }
    }

    private String handleContent(Tokenizer tokenizer) {
        return tokenizer.getStringToken(currentState);
    }


}
