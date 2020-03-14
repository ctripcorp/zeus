package com.ctrip.zeus.util;

import java.io.UnsupportedEncodingException;

/**
 * Created by zhoumy on 2015/12/7.
 */
public class BytesUtils {
    public static String toString(byte bytes[], int offset, int length) {
        try {
            return new String(bytes, offset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes, offset, length);
        }
    }
}
