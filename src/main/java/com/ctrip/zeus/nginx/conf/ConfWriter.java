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

    public static void writeNginxConf(Slb slb, String conf) throws IOException {
        String confDir = slb.getNginxConf();
        Writer writer = null;
        try {
            makeSureExist(confDir);
            writer = new FileWriter(new File(confDir + "/nginx.conf"));
            writer.write(conf);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

    }

    public static void writeServerConf(Slb slb, VirtualServer vs,  String conf) throws IOException {
        String confDir = slb.getNginxConf();
        Writer writer = null;
        try {
            makeSureExist(confDir + "/vhosts");
            writer = new FileWriter(new File(confDir + "/vhosts/" + vs.getName() + ".conf"));
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
