package com.ctrip.zeus.service.rule.model;

import java.util.HashMap;
import java.util.Map;

public enum ConditionFunction {
    EQUAL("="),
    GT(">"),
    LT("<"),
    NOTEQUAL("!="),
    LIKE("~"),
    LIKEIGNORECASE("~*"),
    NOTLIKE("!~"),
    IN("in"),
    NOTLIKEIGNORECASE("!~*");

    private String fn;

    private static Map<String,String> luaFunctionMap = new HashMap<>();
    private static Map<String,String> nginxFunctionMap = new HashMap<>();

    static {
        luaFunctionMap.put("=","==");
        luaFunctionMap.put(">",">");
        luaFunctionMap.put("<","<");
        luaFunctionMap.put("!=","~=");

        nginxFunctionMap.put("~","~");
        nginxFunctionMap.put("~*","~*");
        nginxFunctionMap.put("!~","!~");
        nginxFunctionMap.put("!~*","!~*");

    }


    ConditionFunction(String function) {
        this.fn = function;
    }

    public String getFn() {
        return fn;
    }
    public String getNginxFunc() {
        return nginxFunctionMap.get(fn);
    }
    public String getLuaFunc() {
        return luaFunctionMap.get(fn);
    }

    public String getName() {
        return name();
    }

    public static ConditionFunction getFunction(String fn) {
        ConditionFunction result = null;
        if (fn == null) return result;
        ConditionFunction[] constants = ConditionFunction.class.getEnumConstants();
        for (int i = 0; i < constants.length; i++) {
            if (fn.equalsIgnoreCase(constants[i].getFn())) {
                result = constants[i];
                break;
            }
        }
        return result;
    }

}
