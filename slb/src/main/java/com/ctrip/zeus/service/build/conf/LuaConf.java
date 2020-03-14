package com.ctrip.zeus.service.build.conf;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;

public class LuaConf {

    public static final String GETTIEMOFDAY_FUNCTION = "gettimeofday";
    public static final String TABLE_LEN_FUNCTION = "table_len";
    public static final String COOKIE_LEN_FUNCTION = "cookie_len";

    private static DynamicBooleanProperty buildGetTimeOfDay = DynamicPropertyFactory.getInstance().getBooleanProperty("init.by.lua.gettimeofday.enable", false);

    public static void writeCookieLenFunction(ConfWriter confWriter) {
        confWriter.writeLine("function " + COOKIE_LEN_FUNCTION + "()");
        confWriter.writeLine("if ( ngx.var.http_cookie == nil ) then");
        confWriter.writeLine("return 0");
        confWriter.writeLine("end");
        confWriter.writeLine("return string.len(string.gsub(ngx.var.http_cookie, \"[^;]\", \"\")) + 1");
        confWriter.writeLine("end");
    }

    public static void writeTableLenFunction(ConfWriter confWriter) {
        confWriter.writeLine("function " + TABLE_LEN_FUNCTION + "(t)");
        confWriter.writeLine("local leng=0");
        confWriter.writeLine("for k, v in pairs(t) do");
        confWriter.writeLine("leng=leng+1");
        confWriter.writeLine("end");
        confWriter.writeLine("return leng;");
        confWriter.writeLine("end");
    }

    public static void writeGetTimeFunction(ConfWriter confWriter) {
        if (!buildGetTimeOfDay.get()) {
            return;
        }
        confWriter.writeLine("ffi = require \"ffi\"");
        confWriter.writeLine("C = ffi.C\n" +
                " if pcall(ffi.typeof, \"struct timeval\") then\n" +
                " else\n" +
                "        ffi.cdef[[\n" +
                "        typedef long time_t;\n" +
                "        typedef struct timeval {\n" +
                "                time_t tv_sec;\n" +
                "                time_t tv_usec;\n" +
                "        } timeval;\n" +
                "        int gettimeofday(struct timeval* t, void* tzp);\n" +
                "        ]]\n" +
                "end\n");
        confWriter.writeLine("function " + GETTIEMOFDAY_FUNCTION + "()");
        confWriter.writeLine("local gettimeofday_struct = ffi.new(\"timeval\")");
        confWriter.writeLine("C.gettimeofday(gettimeofday_struct, nil)");
        confWriter.writeLine("return tonumber(gettimeofday_struct.tv_sec) * 1000000 + tonumber(gettimeofday_struct.tv_usec)");
        confWriter.writeLine("end");
    }
}
