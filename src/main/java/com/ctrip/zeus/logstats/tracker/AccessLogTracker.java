package com.ctrip.zeus.logstats.tracker;

import com.ctrip.zeus.logstats.StatsDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;

/**
 * Created by zhoumy on 2015/11/13.
 */
public class AccessLogTracker implements LogTracker {
    private final int TrackLatch = 10;
    private final LogTrackerStrategy strategy;
    private final String logFilename;
    private final int size;
    private final ByteBuffer buffer;
    private RandomAccessFile raf;
    private FileChannel fileChannel;
    private long offset;
    private long previousOffset;
    private String offsetValue = "";
    private int rollingLogCounter;
    private File trackingFile;
    private boolean allowTracking;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public AccessLogTracker(LogTrackerStrategy strategy) {
        this.strategy = strategy;
        logFilename = strategy.getLogFilename();
        size = strategy.getReadSize();
        allowTracking = strategy.isAllowTrackerMemo();
        if (allowTracking) {
            trackingFile = new File(strategy.getTrackerMemoFilename());
            if (!trackingFile.exists())
                try {
                    trackingFile.createNewFile();
                } catch (IOException e) {
                    allowTracking = false;
                    logger.error("Create access log tracking file fails.", e);
                }
        }
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
    public boolean reachFileEnd() throws IOException {
        return offset == fileChannel.size();
    }

    @Override
    public void start() throws IOException {
        raf = new RandomAccessFile(getLogFilename(), "r");
        fileChannel = raf.getChannel();
        if (allowTracking) {
            // init state
            RecordOffset curr = getRecordOffset();
            if (curr.rOffset == 0) {
                previousOffset = offset = 0;
                return;
            }
            // log rotate must be done
            if (fileChannel.size() < curr.rOffset)
                previousOffset = offset = 0;
            else {
                // check if log rotate has been done
                fileChannel.position(curr.rOffset);
                String rafline = raf.readLine();
                if (rafline.equals(curr.rValue)) {
                    previousOffset = curr.rOffset;
                    offset = fileChannel.position();
                    return;
                }
            }
            fileChannel.position(offset);
        }
    }

    @Override
    public void stop() throws IOException {
        rollingLogCounter = TrackLatch;
        tryLog();
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
    public void fastMove(final StatsDelegate<String> delegator) throws IOException {
        try {
            if (offset > fileChannel.size()) {
                previousOffset = offset = 0;
                fileChannel.position(offset);
            }
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
            byte[] line = new byte[size];

            while (buffer.hasRemaining()) {
                while (!eol && buffer.hasRemaining()) {
                    byte b;
                    switch (b = buffer.get()) {
                        case -1:
                        case '\n':
                            eol = true;
                            offsetValue = new String(line, 0, colOffset);
                            delegator.delegate(offsetValue);
                            previousOffset = offset;
                            offset += ++colOffset;
                            break;
                        case '\r':
                            eol = true;
                            offsetValue = new String(line, 0, colOffset);
                            if ((buffer.get()) != '\n')
                                buffer.position(colOffset);
                            else
                                colOffset++;
                            delegator.delegate(offsetValue);
                            previousOffset = offset;
                            offset += ++colOffset;
                            break;
                        default:
                            line[colOffset] = b;
                            ++colOffset;
                            break;
                    } // end of switch
                }// end of while !eol
                colOffset = 0;
                eol = false;
            }
            fileChannel.position(offset);
            tryLog();
        } catch (IOException ex) {
            logger.error("Some error occurred when tracking access.log.", ex);
            hotfix();
        }
    }

    private void hotfix() throws IOException {
        if (fileChannel != null)
            fileChannel.close();
        if (raf != null)
            raf.close();
        fileChannel = null;
        raf = null;
        start();
        fileChannel.position(offset);
    }

    private void tryLog() {
        rollingLogCounter++;
        if (allowTracking && (TrackLatch <= rollingLogCounter)) {
            OutputStream os = null;
            try {
                os = new FileOutputStream(trackingFile);
                String output = Long.valueOf(previousOffset).toString() + "\n" + offsetValue;
                os.write(output.getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                logger.error("Fail to write offset to tracking file.", e);
            } finally {
                if (os != null)
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
            rollingLogCounter = 0;
        }
    }

    private RecordOffset getRecordOffset() {
        RecordOffset result = new RecordOffset();
        result.rOffset = offset;
        if (allowTracking) {
            Scanner scanner = null;
            try {
                scanner = new Scanner(trackingFile);
                if (scanner.hasNextLine())
                    result.rOffset = Integer.parseInt(scanner.nextLine());
                if (scanner.hasNextLine())
                    result.rValue = scanner.nextLine();
            } catch (FileNotFoundException e) {
                result.rOffset = offset;
                e.printStackTrace();
            } finally {
                if (scanner != null)
                    scanner.close();
            }
        }
        return result;
    }

    private class RecordOffset {
        long rOffset;
        String rValue;
    }
}
