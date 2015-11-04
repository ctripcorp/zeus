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
     * Convert InputStream to string.
     *
     * @param is the InputStream
     * @return string from input stream
     * @throws IOException
     */
    public static String inputStreamStringify(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(is, baos);
        return baos.toString();
    }

    public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(is, baos);
        return baos.toByteArray();
    }

    /**
     * Copy InputStream to OutputStream.
     *
     * @param is the InputStream
     * @param os the OutputStream
     * @throws IOException
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[4096];
        int n = 0;
        while ((n = is.read(buffer)) != -1) {
            os.write(buffer, 0, n);
        }
    }
}
