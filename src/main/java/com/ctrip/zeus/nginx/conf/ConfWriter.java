package com.ctrip.zeus.nginx.conf;

import com.ctrip.zeus.model.entity.Slb;
import com.ctrip.zeus.model.entity.VirtualServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author:xingchaowang
 * @date: 3/8/2015.
 */
public class ConfWriter {

    public static void writeNginxConf(String path, String conf) throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriter(new File(path));
            writer.write(conf);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void writeServerConf(String path, String conf) throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriter(new File(path));
            writer.write(conf);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public static void writeUpstreamsConf(String path, String conf) throws IOException {
        Writer writer = null;
        try {
            writer = new FileWriter(new File(path));
            writer.write(conf);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void makeSureExist(String confDir) {
        File f = new File(confDir);
        f.mkdirs();
    }
}
