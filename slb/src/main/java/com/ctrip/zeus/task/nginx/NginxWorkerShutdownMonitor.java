package com.ctrip.zeus.task.nginx;

import com.ctrip.zeus.service.build.ConfigHandler;
import com.ctrip.zeus.support.ObjectJsonWriter;
import com.ctrip.zeus.task.AbstractTask;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("nginxWorkerShutdownMonitor")
public class NginxWorkerShutdownMonitor extends AbstractTask {

    @Resource
    private ConfigHandler configHandler;

    private HashMap<String, Long> pidMap = new HashMap<>();
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start() {

    }

    @Override
    public void run() throws Exception {
        Set<String> pids = getToBeShutDownWorker();
        if (pids == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Set<String> old = new HashSet<>(pidMap.keySet());
        old.removeAll(pids);
        for (String id : old) {
            pidMap.remove(id);
        }

        for (String pid : pids) {
            if (!pidMap.containsKey(pid)) {
                pidMap.put(pid, now);
            }
        }

        logger.info("[[NginxShutdownWorker=true]]Check Result:" + ObjectJsonWriter.write(pidMap));

        if (configHandler.getEnable("kill.nginx.woker.shutdown", true)) {
            for (String pid : pidMap.keySet()) {
                long existTime = now - pidMap.get(pid);
                if (existTime / 1000 >= configHandler.getIntValue("kill.nginx.woker.shutdown.timeout", 60 * 10)) {
                    killPid(pid);
                }
            }
        }
    }

    private void killPid(String pid) {
        long start = System.currentTimeMillis();
        try {
            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(null);

            String killCommand = "sudo kill -9 " + pid;
            ByteArrayOutputStream killOutputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream killErrorStream = new ByteArrayOutputStream();
            CommandLine c = CommandLine.parse(killCommand);

            PumpStreamHandler killStreamHandler = new PumpStreamHandler(killOutputStream, killErrorStream);
            exec.setStreamHandler(killStreamHandler);
            logger.info("[[NginxWorker=kill]]Kill Started:" + pid);
            exec.execute(c);
            logger.info("[[NginxWorker=kill]]Kill Finished:" + pid + "Out:" + killOutputStream.toString("UTF-8")
                    + "Out:" + killErrorStream.toString("UTF-8"));
        } catch (IOException e) {
            logger.error("Fail to shutdown nginx worker", e);
        } finally {
            long cost = System.currentTimeMillis() - start;
            logger.info("[NginxWorkerKill] Finish Kill Nginx Workers .Cost:" + cost);
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public long getInterval() {
        return 60000;
    }


    private Set<String> getToBeShutDownWorker() {
        long start = System.currentTimeMillis();
        try {
            String command = "sudo ps -aux";
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            CommandLine commandline = CommandLine.parse(command);

            DefaultExecutor exec = new DefaultExecutor();
            exec.setExitValues(null);

            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
            exec.setStreamHandler(streamHandler);

            int exitVal = exec.execute(commandline);
            String out = outputStream.toString("UTF-8");
            String error = errorStream.toString("UTF-8");
            boolean success = (0 == exitVal);
            logger.info("[GetNginxWorker]Shell Result. Status:" + exitVal + "\nOut:" + out + "\nError:" + error);
            if (!success) {
                return null;
            }
            return parserWorkerPid(out);
        } catch (IOException e) {
            logger.error("Fail to get tobe shutdown nginx worker", e);
            return null;
        } finally {
            long cost = System.currentTimeMillis() - start;
            logger.info("[NginxWorkerKill] Finish Get Nginx Worker PID .Cost:" + cost);
        }
    }

    public Set<String> parserWorkerPid(String out) {
        Set<String> result = new HashSet<>();
        if (out == null || out.isEmpty()) {
            return result;
        }
        String[] lines = out.split("\n");
        Pattern pattern = Pattern.compile("nobody( +)(\\d+)( +).*");
        for (String line : lines) {
            if (!line.contains(" nginx: worker process is shutting down")) {
                continue;
            }
            if (!line.startsWith("nobody")) {
                continue;
            }
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                String pid = m.group(2);
                try {
                    Long.parseLong(pid);
                    result.add(pid);
                } catch (Exception e) {
                    logger.info("Parser Pid Failed.Out:" + out + ";PID:" + pid);
                }
            }
        }
        return result;
    }

}
