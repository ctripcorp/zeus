package com.ctrip.zeus.util;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public final class Cipher {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static String encode(String src) {
        int seed = new Random().nextInt(5) + 3;
        return encode(src, seed, seed / 2 + 1, seed * 2 + 1);
    }

    public static String decode(String src) {
        int len = src.length();
        ByteBuffer bb = ByteBuffer.allocate((len - 3) / 2);
        int step = Character.digit(src.charAt(0), 16) & 0x07;
        int offset = Character.digit(src.charAt(1), 16);
        int mask = Character.digit(src.charAt(2), 16);

        for (int i = 3; i < len; i += 2) {
            byte high = (byte) (Character.digit(src.charAt(i), 16) & 0xFF);
            byte low = (byte) (Character.digit(src.charAt(i + 1), 16) & 0xFF);
            bb.put((byte) (high << 4 | low));
        }

        byte[] data = (byte[]) bb.flip().array();

        mask(data, mask);
        swap(data, step, offset);

        return new String(data, 0, data.length - 13, CHARSET);
    }

    private static String encode(String src, int step, int offset, int mask) {
        byte[] data = padding(src);
        swap(data, step, offset);
        mask(data, mask);
        return wrapUp(data, step, offset, mask);
    }

    private static void mask(byte[] data, int mask) {
        for (int i = data.length - 1; i >= 0; i--) {
            data[i] ^= mask;
        }
    }

    private static void swap(byte[] data, int step, int offset) {
        int len = data.length * 8;

        for (int i = 0; i < len; i += step) {
            int j = i + offset;

            if (j < len) {
                byte b1 = data[i / 8];
                byte b2 = data[j / 8];
                int f1 = b1 & (1 << (i % 8));
                int f2 = b2 & (1 << (j % 8));

                if ((f1 != 0) != (f2 != 0)) {
                    data[i / 8] ^= 1 << (i % 8);
                    data[j / 8] ^= 1 << (j % 8);
                }
            }
        }
    }

    private static byte[] padding(String str) {
        byte[] data = str.getBytes(CHARSET);
        ByteBuffer bb = ByteBuffer.allocate(data.length + 13);

        bb.put(data);
        bb.put((byte) 0);
        try {
            bb.put(Inet4Address.getLocalHost().getAddress());
        } catch (UnknownHostException e) {
            bb.putLong(0x7f000001); // 127.0.0.1
        }
        bb.putLong(System.currentTimeMillis());

        return (byte[]) bb.flip().array();
    }

    private static String wrapUp(byte[] data, int step, int offset, int mask) {
        StringBuilder sb = new StringBuilder(data.length * 2 + 3);
        sb.append(Integer.toHexString(step | 0x08));
        sb.append(Integer.toHexString(offset));
        sb.append(Integer.toHexString(mask));
        for (byte d : data) {
            sb.append(Integer.toHexString(d >> 4 & 0x0F));
            sb.append(Integer.toHexString(d & 0x0F));
        }
        return sb.toString();
    }
}

