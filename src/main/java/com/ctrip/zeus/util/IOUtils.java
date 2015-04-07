package com.ctrip.zeus.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by zhoumy on 2015/4/7.
 */
public class IOUtils {
    /**
     * convert input stream to string
     * @param is the input stream
     * @return string from input stream
     * @throws IOException
     */
    public static String inputStreamStringify(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;

        while ((n = is.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toString();
    }
}
