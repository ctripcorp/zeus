package com.ctrip.zeus.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by zhoumy on 2016/4/26.
 */
public class CompressUtils {
    public static byte[] compress(String value) throws IOException {
        if (value == null || value.isEmpty()) return new byte[0];
        ByteArrayOutputStream os = new ByteArrayOutputStream(value.length());
        GZIPOutputStream gzip = new GZIPOutputStream(os);
        try {
            gzip.write(value.getBytes());
        } finally {
            gzip.close();
        }
        return os.toByteArray();
    }

    public static String decompress(byte[] value) throws IOException {
        if (value == null || value.length == 0) return "";
        ByteArrayInputStream is = new ByteArrayInputStream(value);
        try {
            if (isCompressed(value)) {
                GZIPInputStream gzip = new GZIPInputStream(is);
                try {
                    return IOUtils.inputStreamStringify(gzip);
                } finally {
                    gzip.close();
                }
            }
            return IOUtils.inputStreamStringify(is);
        } finally {
            is.close();
        }
    }

    public static boolean isCompressed(byte[] value) {
        return (value[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (value[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
