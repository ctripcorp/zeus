package com.ctrip.zeus.logstats.tracker;

import com.ctrip.zeus.logstats.StatsDelegate;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by zhoumy on 2015/11/13.
 */
public class AccessLogTracker implements LogTracker {
    private final LogTrackerStrategy strategy;
    private final String logFilename;
    private final int size;
    private final ByteBuffer buffer;
    private RandomAccessFile raf;
    private FileChannel fileChannel;

    public AccessLogTracker(LogTrackerStrategy strategy) {
        this.strategy = strategy;
        this.logFilename = strategy.getLogFilename();
        this.size = strategy.getReadSize();
        buffer = ByteBuffer.allocate(size);
    }

    @Override
    public String getName() {
        return "AccessLogTracker";
    }

    @Override
    public String getLogFilename() {
        return logFilename;
    }

    @Override
    public LogTrackerStrategy getStrategy() {
        return strategy;
    }

    @Override
    public void start() throws IOException {
        raf = new RandomAccessFile(getLogFilename(), "r");
        fileChannel = raf.getChannel();
    }

    @Override
    public void stop() throws IOException {
        if (fileChannel != null)
            fileChannel.close();
        if (raf != null)
            raf.close();
        fileChannel = null;
        raf = null;
    }

    @Override
    public String move() throws IOException {
        return raf.readLine();
    }

    @Override
    public void fastMove(final StatsDelegate<String> delegate) throws IOException {
        buffer.clear();
        try {
            if (fileChannel.read(buffer) == -1)
                return;
        } catch (IOException ex) {
            stop();
        }
        buffer.flip();
        boolean eol = false;
        int colOffset = 0;
        int lnOffset = 0;

        byte[] line = new byte[size];
        while (buffer.hasRemaining()) {
            while (!eol) {
                byte b;
                switch (b = buffer.get()) {
                    case -1:
                    case '\n':
                        eol = true;
                        delegate.delegate(new String(line, 0, colOffset));
                        lnOffset = ++colOffset;
                        break;
                    case '\r':
                        eol = true;
                        if ((buffer.get()) != '\n')
                            buffer.position(colOffset);
                        else
                            colOffset++;
                        delegate.delegate(new String(line, 0, colOffset));
                        lnOffset = ++colOffset;
                        break;
                    default:
                        line[colOffset] = b;
                        ++colOffset;
                        break;
                } // end of switch
            }// end of while !eol
        }
        fileChannel.position(lnOffset);
    }
}
