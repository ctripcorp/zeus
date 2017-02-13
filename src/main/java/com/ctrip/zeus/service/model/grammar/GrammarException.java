package com.ctrip.zeus.service.model.grammar;

/**
 * Created by zhoumy on 2017/1/26.
 */
public class GrammarException extends Exception {
    public GrammarException(String message) {
        super(message);
    }

    public GrammarException(Throwable throwable) {
        super(throwable);
    }
}
