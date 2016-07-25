package com.ctrip.zeus.support;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Created by zhoumy on 2016/7/25.
 */
public class LowerCaseWithHyphenStrategy extends PropertyNamingStrategy.PropertyNamingStrategyBase {
    public LowerCaseWithHyphenStrategy() {
    }

    public String translate(String input) {
        if (input == null) {
            return input;
        } else {
            int length = input.length();
            StringBuilder result = new StringBuilder(length * 2);
            int resultLength = 0;
            boolean wasPrevTranslated = false;

            for (int i = 0; i < length; ++i) {
                char c = input.charAt(i);
                if (i > 0 || c != 95) {
                    if (Character.isUpperCase(c)) {
                        if (!wasPrevTranslated && resultLength > 0 && result.charAt(resultLength - 1) != 95) {
                            result.append('-');
                            ++resultLength;
                        }

                        c = Character.toLowerCase(c);
                        wasPrevTranslated = true;
                    } else {
                        wasPrevTranslated = false;
                    }

                    result.append(c);
                    ++resultLength;
                }
            }

            return resultLength > 0 ? result.toString() : input;
        }
    }
}
