package com.ctrip.zeus.service.model;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.dal.core.RGroupVsDao;
import com.ctrip.zeus.dal.core.RelGroupVsDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.util.StringUtils;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhoumy on 2016/3/31.
 */
public class PathValidationTest extends AbstractServerTest {
    @Resource
    private SlbRepository slbRepository;

    @Resource
    private RGroupVsDao rGroupVsDao;

    @Resource
    private GroupValidator groupModelValidator;

    @Test
    public void testStringUtils() {
        String s1 = "abcdefg";
        String s2 = "abc";
        String s3 = "ABCDefghij($|/|\\?)";
        String s4 = "我爱中国";
        String s5 = "我($|/|\\?)";
        String s6 = "我爱中国中国爱我";

        String s7 = "bcdef($|/|\\?)";
        String s8 = "爱";

        Assert.assertTrue(StringUtils.prefixOverlapped(s1, s2, '(') == 1);
        Assert.assertTrue(StringUtils.prefixOverlapped(s2, s3, '(') == 2);
        Assert.assertTrue(StringUtils.prefixOverlapped(s3, s1, '(') == 1);

        Assert.assertTrue(StringUtils.prefixOverlapped(s4, s5, '(') == 1);
        Assert.assertTrue(StringUtils.prefixOverlapped(s5, s6, '(') == 2);
        Assert.assertTrue(StringUtils.prefixOverlapped(s6, s4, '(') == 1);

        Assert.assertTrue(StringUtils.prefixOverlapped(s1, s7, '(') == -1);
        Assert.assertTrue(StringUtils.prefixOverlapped(s4, s8, '(') == -1);
        Assert.assertTrue(StringUtils.prefixOverlapped(s1, s4, '(') == -1);
    }

    @Test
    public void testAddExistingPath() {
        String path = "~* ^/linkservice($|/|\\?)";
        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
    }

    @Test
    public void testAddNormalPath() {
        String path = "~* ^/normal($|/|\\?)";
        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testAddOverlappingPath() {
        String path = "~* ^/linkservice/config($|/|\\?)";
        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
    }

    @Test
    public void testUpdatePath() {
        String path = "~* ^/baike($|/|\\?)";
        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(10L, array);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testAddUpperCasePath() {
        String path = "~* ^/BAIKE/CONFIG($|/|\\?)";
        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
    }

    @Test
    public void testUnconventionalPath() {
        String root = "/";
        String startWith = "^/CommentAdmin";
        String noStart = "baike";
        String noAlphabetic = "/123";
        String success = "success";
        String empty = "";

        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(root).setVirtualServer(new VirtualServer().setId(1L)));

        try {
            groupModelValidator.validateGroupVirtualServers(100L, array);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPath(success);
            groupModelValidator.validateGroupVirtualServers(100L, array);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPath(startWith);
            groupModelValidator.validateGroupVirtualServers(100L, array);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        try {
            array.get(0).setPath(noStart);
            groupModelValidator.validateGroupVirtualServers(100L, array);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        try {
            array.get(0).setPath(noAlphabetic);
            groupModelValidator.validateGroupVirtualServers(100L, array);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        try {
            array.get(0).setPath(empty);
            groupModelValidator.validateGroupVirtualServers(100L, array);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
    }

    @Before
    public void check() throws Exception {
        Slb slb = slbRepository.getById(1L);
        Set<String> existingPaths = Sets.newHashSet("~* /", "~* ^/CommentAdmin/", "~* ^/baike($|/|\\?)",
                "~* ^/123likectrip($|/|\\?)",
                "~* ^/linkservice($|/|\\?)", "~* ^/linkservice/link($|/|\\?)",
                "~* ^/tour-marketingservice($|/|\\?)", "~* ^/tour-MarketingServiceConfig($|/|\\?)",
                "~* ^/cruise-interface-costa($|/|\\?)", "~* ^/Cruise-Product-WCFService($|/|\\?)", "~* ^/Cruise-Product-OctopusJob($|/|\\?)");
        if (slb == null) {
            slb = new Slb().setName("default").setStatus("TEST")
                    .addVip(new Vip().setIp("10.2.25.93"))
                    .addSlbServer(new SlbServer().setIp("10.2.25.93").setHostName("uat0358"))
                    .addVirtualServer(new VirtualServer().setName("defaultSlbVs1").setSsl(false).setPort("80")
                            .addDomain(new Domain().setName("defaultSlbVs1.ctrip.com")));
            slbRepository.add(slb);

            for (String path : existingPaths) {
                rGroupVsDao.insert(new RelGroupVsDo().setGroupId(10).setVsId(1).setPath(path));
            }
        }
    }
}
