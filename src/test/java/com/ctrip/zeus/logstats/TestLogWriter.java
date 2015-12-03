package com.ctrip.zeus.logstats;

import com.google.common.base.Joiner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Created by zhoumy on 2015/11/30.
 */
public class TestLogWriter {
    private final String logFilename;
    private final long logRotateInterval;
    private RandomAccessFile raf;
    private FileChannel fileChannel;
    private int count;

    public TestLogWriter(String logFilename, long logRotateInterval) {
        this.logFilename = logFilename;
        this.logRotateInterval = logRotateInterval;
        File f = new File(logFilename);
        try {
            if (!f.exists())
                f.createNewFile();
            raf = new RandomAccessFile(logFilename, "rw");
            fileChannel = raf.getChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String randomGenRec() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        Integer[] ipComponents1 = {rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)};
        Integer[] ipComponents2 = {rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)};
        Integer[] ipComponents3 = {rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)};
        Integer[] ipComponents4 = {rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)};
        sb.append("[").append(new Date().toString()).append("]")
                .append(" ").append("zeus.ctrip.com." + rand.nextInt(10))
                .append(" ").append("vms" + rand.nextLong())
                .append(" ").append(Joiner.on(".").join(ipComponents1))
                .append(" ").append("GET")
                .append(" ").append(randomGenUri(rand.nextInt(10)))
                .append(" \"-\" 80 -")
                .append(" ").append(Joiner.on(".").join(ipComponents2))
                .append(" ").append(Joiner.on(".").join(ipComponents3))
                .append(" HTTP/1.1 \"-\" \"-\" \"-\"")
                .append(" ").append("zeus.ctrip.com." + rand.nextInt(10))
                .append(" 200").append(" " + rand.nextLong())
                .append(" " + rand.nextDouble()).append(" " + rand.nextDouble())
                .append(" ").append(Joiner.on(".").join(ipComponents4) + ":80")
                .append(" 200\n");
        return sb.toString();
    }

    public void run(long endTime) throws Exception {
        if (raf == null || fileChannel == null)
            return;
        while (System.currentTimeMillis() <= endTime) {
            long nextRotateTime = System.currentTimeMillis() + logRotateInterval;
            long now;
            while ((now = System.currentTimeMillis()) <= endTime && now <= nextRotateTime) {
                fileChannel.write(ByteBuffer.wrap(randomGenRec().getBytes(Charset.forName("UTF-8"))));
                count++;
            }
            if (now > nextRotateTime) {
                Thread.sleep(500L);
                try {
                    fileChannel.truncate(0);
                    fileChannel.close();
                    raf.close();
                    fileChannel = null;
                    raf = null;
                } finally {
                    raf = new RandomAccessFile(logFilename, "rw");
                    fileChannel = raf.getChannel();
                    System.out.println("Log rotate has been done.");
                }
            }
        }
    }

    public void stop() throws IOException {
        fileChannel.close();
        raf.close();
        fileChannel = null;
        raf = null;
    }

    public int getCount() {
        return count;
    }

    private static String randomGenUri(int loop) {
        loop = loop >= 1 ? loop : 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < loop; i++) {
            sb.append(UUID.randomUUID().toString().replaceAll("-", "/"));
        }
        return "/" + sb.toString();
    }
}