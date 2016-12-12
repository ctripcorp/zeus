package com.ctrip.zeus.util;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    public static String compressToGzippedBase64String(String value) throws IOException {
        if (value == null || value.isEmpty()) value = "";
        ByteArrayOutputStream os = new ByteArrayOutputStream(value.length());
        Base64OutputStream base64os = new Base64OutputStream(os);
        GZIPOutputStream gzip = new GZIPOutputStream(base64os);
        try {
            gzip.write(value.getBytes(StandardCharsets.UTF_8));
        } finally {
            gzip.close();
            base64os.close();
        }
        return os.toString();
    }


    public static String decompressGzippedBase64String(String value) throws IOException {
        if (value == null) return null;
        byte[] base64 = org.apache.commons.codec.binary.Base64.decodeBase64(value.getBytes());
        ByteArrayInputStream is = new ByteArrayInputStream(base64);
        try {
            if (isCompressed(base64)) {
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
