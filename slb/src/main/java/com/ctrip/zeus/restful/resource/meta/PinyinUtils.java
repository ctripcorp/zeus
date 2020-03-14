package com.ctrip.zeus.restful.resource.meta;

import com.github.promeg.pinyinhelper.Pinyin;

/**
 * @author:xingchaowang
 * @date: 2016/8/17.
 */
public class PinyinUtils {

    static public String getPinyin(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder r1 = new StringBuilder(128);
        StringBuilder r2 = new StringBuilder(128);
        String r3 = "";
        for (char c : s.toCharArray()) {
            String p = Pinyin.toPinyin(c);
            r1.append(p.substring(0, 1));
            r2.append(p);
        }
        return (r1.toString() + r2.toString() + r3).toLowerCase();
    }
}
