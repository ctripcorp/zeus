package com.ctrip.zeus.service;

import com.ctrip.zeus.client.GroupClient;
import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.transform.DefaultSaxParser;
import com.ctrip.zeus.support.GenericSerializer;
import com.ctrip.zeus.support.ObjectJsonParser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoumy on 2016/1/25.
 */
public class DalParsingPerformanceTest {

    private static List<String> xmlData = new ArrayList<>();
    private static List<String> jsonData = new ArrayList<>();

    private static List<String> xmlCompactData = new ArrayList<>();
    private static List<String> jsonCompactData = new ArrayList<>();

    @BeforeClass
    public static void loadData() {
        GroupClient groupClient = new GroupClient("http://10.2.25.93:8099");
        for (Group g : groupClient.getAll()) {
            xmlData.add(GenericSerializer.writeXml(g));
            xmlCompactData.add(GenericSerializer.writeXml(g, false));
            jsonData.add(GenericSerializer.writeJson(g));
            jsonCompactData.add(GenericSerializer.writeJson(g, false));
        }
    }

    @Test
    public void testSaxParsing() throws Exception {
        // warm up
        for (int i = 0; i < 50; i++) {
            for (String s : xmlData) {
                parseXml(s);
            }
        }
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            for (String s : xmlData) {
                parseXml(s);
            }
        }

        long duration = (System.nanoTime() - start) / 1000L;
        long totalCost = duration / 100;
        long perCost = totalCost / xmlData.size();

        System.out.println("Parse xml data: total " + xmlData.size() + " uat group costs " + ((double) totalCost / 1000.0) + " ms.");
        System.out.println("Parse xml data: one group costs " + ((double) perCost / 1000.0) + " ms.");
    }

    @Test
    public void testSaxCompactParsing() throws Exception {
        // warm up
        for (int i = 0; i < 50; i++) {
            for (String s : xmlCompactData) {
                parseXml(s);
            }
        }
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            for (String s : xmlCompactData) {
                parseXml(s);
            }
        }

        long duration = (System.nanoTime() - start) / 1000L;
        long totalCost = duration / 100;
        long perCost = totalCost / xmlCompactData.size();

        System.out.println("Parse xml compact data: total " + xmlCompactData.size() + " uat group costs " + ((double) totalCost / 1000.0) + " ms.");
        System.out.println("Parse xml compact data: one group costs " + ((double) perCost / 1000.0) + " ms.");
    }

    @Test
    public void testJsonParsing() throws Exception {
        // warm up
        for (int i = 0; i < 50; i++) {
            for (String s : jsonData) {
                parseJson(s);
            }
        }
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            for (String s : jsonData) {
                parseJson(s);
            }
        }

        long duration = (System.nanoTime() - start) / 1000L;
        long totalCost = duration / 100;
        long perCost = totalCost / jsonData.size();

        System.out.println("Parse json data: total " + jsonData.size() + " uat group costs " + ((double) totalCost / 1000.0) + " ms.");
        System.out.println("Parse json data: one group costs " + ((double) perCost / 1000.0) + " ms.");
    }

    @Test
    public void testJsonCompactParsing() throws Exception {
        // warm up
        for (int i = 0; i < 50; i++) {
            for (String s : jsonCompactData) {
                parseJson(s);
            }
        }
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            for (String s : jsonCompactData) {
                parseJson(s);
            }
        }

        long duration = (System.nanoTime() - start) / 1000L;
        long totalCost = duration / 100;
        long perCost = totalCost / jsonCompactData.size();

        System.out.println("Parse json compact data: total " + jsonCompactData.size() + " uat group costs " + ((double) totalCost / 1000.0) + " ms.");
        System.out.println("Parse json compact data: one group costs " + ((double) perCost / 1000.0) + " ms.");
    }

    @Test
    public void testJacksonJsonParsing() throws Exception {
        // warm up
        for (int i = 0; i < 50; i++) {
            for (String s : jsonData) {
                Assert.assertNotNull(ObjectJsonParser.parse(s, Group.class));
            }
        }
        long start = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            for (String s : jsonData) {
                parseJson(s);
            }
        }

        long duration = (System.nanoTime() - start) / 1000L;
        long totalCost = duration / 100;
        long perCost = totalCost / jsonData.size();

        System.out.println("Parse json data using jackson: total " + jsonData.size() + " uat group costs " + ((double) totalCost / 1000.0) + " ms.");
        System.out.println("Parse json data using jackson: one group costs " + ((double) perCost / 1000.0) + " ms.");
    }

    private void parseXml(String data) throws IOException, SAXException {
        Assert.assertNotNull(DefaultSaxParser.parseEntity(Group.class, data));
    }

    private void parseJson(String data) throws IOException {
        Assert.assertNotNull(ObjectJsonParser.parse(data, Group.class));
    }
}
