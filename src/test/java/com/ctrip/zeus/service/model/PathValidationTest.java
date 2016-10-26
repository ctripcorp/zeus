package com.ctrip.zeus.service.model;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.dal.core.RGroupVsDao;
import com.ctrip.zeus.dal.core.RelGroupVsDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.handler.impl.DefaultGroupValidator;
import com.ctrip.zeus.util.PathUtils;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by zhoumy on 2016/3/31.
 */
public class PathValidationTest extends AbstractServerTest {
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private VirtualServerRepository virtualServerRepository;
    @Resource
    private GroupValidator groupModelValidator;
    @Resource
    private RGroupVsDao rGroupVsDao;

    private final String standardSuffix = "($|/|\\?)";

    @Test
    public void testExtractPath() throws ValidationException {
        String normalValue1 = "abc($|/|\\?)";
        String normalValue2 = "abc";
        String normalValue3 = "/abc";
        String normalValue4 = "^/abc";
        String normalValue5 = "~* /\"abc\"";
        String normalValue6 = "~* \"/abc\"";
        String normalValue7 = "~* /abc($|/|\\?)";
        String creepyValue1 = "\"/abc\"";
        String creepyValue2 = "\"^/\\\"abc\\\"\"";
        String creepyValue3 = "~* \"^/members($|/|\\?)|membersite($|/|\\?)\"";
        String root1 = "~* /";
        String root2 = "/";
        String root3 = "~* ^/";
        String root4 = "~* ^\"/\"";
        String root5 = "~* \"^/\"";
        Assert.assertEquals("abc($|/|\\?)", extractValue(normalValue1));
        Assert.assertEquals("abc", extractValue(normalValue2));
        Assert.assertEquals("abc", extractValue(normalValue3));
        Assert.assertEquals("abc", extractValue(normalValue4));
        Assert.assertEquals("abc", extractValue(normalValue5));
        Assert.assertEquals("abc", extractValue(normalValue6));
        Assert.assertEquals("abc($|/|\\?)", extractValue(normalValue7));
        Assert.assertEquals("abc", extractValue(creepyValue1));
        Assert.assertEquals("\\\"abc\\\"", extractValue(creepyValue2));
        Assert.assertEquals("members($|/|\\?)|membersite($|/|\\?)", extractValue(creepyValue3));
        Assert.assertEquals("/", extractValue(root1));
        Assert.assertEquals("/", extractValue(root2));
        Assert.assertEquals("/", extractValue(root3));
        Assert.assertEquals("/", extractValue(root4));
        Assert.assertEquals("/", extractValue(root5));
    }

    @Test
    public void testPathUtils() throws ValidationException {
        String s1 = "abcdefg";
        String s2 = "abc";
        String s3 = extractValue("ABCDefghij($|/|\\?)");
        String s4 = "我爱中国";
        String s5 = extractValue("我($|/|\\?)");
        String s6 = "我爱中国中国爱我";

        String s7 = extractValue("bcdef($|/|\\?)");
        String s8 = "爱";

        String s9 = "~* \"^/members($|/|\\?)|membersite($|/|\\?)\"";
        String s10 = "~* \"^/members($|/|\\?)membersite($|/|\\?)\"";

        Assert.assertTrue(PathUtils.prefixOverlapped(s1, s2, standardSuffix) == 1);
        Assert.assertTrue(PathUtils.prefixOverlapped(s2, s3, standardSuffix) == 2);
        Assert.assertTrue(PathUtils.prefixOverlapped(s3, s1, standardSuffix) == 1);

        Assert.assertTrue(PathUtils.prefixOverlapped(s4, s5, standardSuffix) == 1);
        Assert.assertTrue(PathUtils.prefixOverlapped(s5, s6, standardSuffix) == 2);
        Assert.assertTrue(PathUtils.prefixOverlapped(s6, s4, standardSuffix) == 1);

        Assert.assertTrue(PathUtils.prefixOverlapped(s1, s7, standardSuffix) == -1);
        Assert.assertTrue(PathUtils.prefixOverlapped(s4, s8, standardSuffix) == -1);
        Assert.assertTrue(PathUtils.prefixOverlapped(s1, s4, standardSuffix) == -1);

        Assert.assertTrue(PathUtils.prefixOverlapped(s3, s3, standardSuffix) == 0);
        Assert.assertTrue(PathUtils.prefixOverlapped(s4, s4, standardSuffix) == 0);

        Assert.assertTrue(PathUtils.prefixOverlapped(s9, s10, standardSuffix) == -1);
    }

    @Test
    public void testRegexSplit() {
        String s1 = "Thingstodo-Order-OrderService($|/|\\?)";
        String s2 = "Thingstodo-Order-OrderService/|^/Thingstodo-Order-OrderService($|/|\\?)";
        String s3 = "(restapi|html5|market|webapp)($|/|\\?)";
        String s4 = "members($|/|\\?)|membersite($|/|\\?)";

        DefaultGroupValidator tmp = (DefaultGroupValidator) groupModelValidator;
        Pattern p = Pattern.compile("^((\\w|-)+/?)(\\$|\\\\\\?)?");
        List<String> r;
        try {
            r = tmp.regexLevelSplit(s1, 1);
            Assert.assertEquals(3, r.size());
            Assert.assertEquals("Thingstodo-Order-OrderService$", r.get(0));
            Assert.assertEquals("Thingstodo-Order-OrderService/", r.get(1));
            Assert.assertEquals("Thingstodo-Order-OrderService\\?", r.get(2));

            Assert.assertTrue(p.matcher(r.get(0)).matches());
            Assert.assertTrue(p.matcher(r.get(1)).matches());
            Assert.assertTrue(p.matcher(r.get(2)).matches());
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

        try {
            r = tmp.regexLevelSplit(s2, 1);
            Assert.assertEquals(6, r.size());
            Assert.assertTrue(p.matcher(r.get(0)).matches());
            Assert.assertFalse(p.matcher(r.get(1)).matches());
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

        try {
            r = tmp.regexLevelSplit(s3, 1);
            Assert.assertEquals(12, r.size());
            Assert.assertEquals("restapi$", r.get(0));
            Assert.assertEquals("restapi/", r.get(1));
            Assert.assertEquals("restapi\\?", r.get(2));
            Assert.assertEquals("html5$", r.get(3));
            Assert.assertEquals("html5/", r.get(4));
            Assert.assertEquals("html5\\?", r.get(5));
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

        try {
            r = tmp.regexLevelSplit(s4, 1);
            Assert.assertEquals(6, r.size());
            Assert.assertEquals("members$", r.get(0));
            Assert.assertEquals("members/", r.get(1));
            Assert.assertEquals("members\\?", r.get(2));
            Assert.assertEquals("membersite$", r.get(3));
            Assert.assertEquals("membersite/", r.get(4));
            Assert.assertEquals("membersite\\?", r.get(5));
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testAddExistingPath() {
        String samePath = "~* ^/linkservice($|/|\\?)";
        String partialPath = "~* ^/members($|/|\\?)";

        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(samePath).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        array.get(0).setPriority(null).setPath(partialPath);
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
    }

    @Test
    public void testAddNormalPath() {
        String path = "~* ^/normal($|/|\\?)";
        String regexRoot = "~* /";

        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(array.get(0).getPriority().intValue() == 1000);
            Assert.assertEquals(path, array.get(0).getPath());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        array.get(0).setPriority(null).setPath(regexRoot);
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertEquals(-1000, array.get(0).getPriority().intValue());
            Assert.assertEquals(regexRoot, array.get(0).getPath());
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
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(array.get(0).getPriority().intValue() == 1100);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPriority(2000);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(array.get(0).getPriority().intValue() == 2000);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testUpdatePath() {
        String path = "~* ^/baike($|/|\\?)";
        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(10L, array, false);
            Assert.assertEquals("~* ^/baike($|/|\\?)", array.get(0).getPath());
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
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(array.get(0).getPriority().intValue() == 1100);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testAddRootPath() {
        String path = "~* /";
        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setPriority(1000).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        array.get(0).setPriority(null);
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(array.get(0).getPriority().intValue() == -1000);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        testAddNormalPath();
    }

    @Test
    public void testUnconventionalPath() {
        String root = "/";
        String startWith = "^/CommentAdmin";
        String noStart = "baik";
        String noAlphabetic = "/123";
        String success = "success";
        String empty = "";
        String specialCase = "~* \"^/(thematic|topic)\"";
        String noMeaningSuffix = "~* \"^/members($|/|\\?)membersite($|/|\\?)\"";

        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(root).setVirtualServer(new VirtualServer().setId(1L)));

        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPriority(null).setPath(success);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPriority(null).setPath(startWith);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertEquals(900, array.get(0).getPriority().intValue());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPriority(null).setPath(noStart);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertEquals(900, array.get(0).getPriority().intValue());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPath(noAlphabetic);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertEquals(900, array.get(0).getPriority().intValue());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPath(empty);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        try {
            array.get(0).setPath(specialCase);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertEquals(900, array.get(0).getPriority().intValue());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            array.get(0).setPath(noMeaningSuffix);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        try {
            array.get(0).setPriority(null).setPath(noMeaningSuffix);
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
        }
    }

    @Test
    public void testRegexExpression() {
        String path = "~* ^/ regex($|/|\\?)";
        List<GroupVirtualServer> array = new ArrayList<>();
        array.add(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        array.get(0).setPath("~= /regex");
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        array.get(0).setPath("=    /exact   ");
        try {
            groupModelValidator.validateGroupVirtualServers(100L, array, false);
            Assert.assertEquals("= /exact", array.get(0).getPath());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Before
    public void check() throws Exception {
        Slb slb = slbRepository.getById(1L);
        Set<String> existingPaths = Sets.newHashSet("~* ^/CommentAdmin/", "~* ^/baike($|/|\\?)",
                "~* ^/123likectrip($|/|\\?)",
                "~* ^/linkservice($|/|\\?)", "~* ^/linkservice/link($|/|\\?)",
                "~* ^/tour-marketingservice($|/|\\?)", "~* ^/tour-MarketingServiceConfig($|/|\\?)",
                "~* ^/cruise-interface-costa($|/|\\?)", "~* ^/Cruise-Product-WCFService($|/|\\?)", "~* ^/Cruise-Product-OctopusJob($|/|\\?)",
                "~* ^/members($|/|\\?)|membersite($|/|\\?)",
                "~* \"^/(journals.aspx$|show(journal)-d([0-9]+)-([a-z,A-z])([0-9]{0,15})([a-z,A-Z,0-9,\\-]{0,20})-([a-z,A-z,0-9,\\:]{0,90}).html$|travels($|/|\\?)|members/journals/add-travels/?$|travel($|/|\\?)|add-travel($|/|\\?)|travelsite($|/|\\?))\"");
        if (slb == null) {
            slb = new Slb().setName("default").setStatus("TEST")
                    .addVip(new Vip().setIp("10.2.25.93"))
                    .addSlbServer(new SlbServer().setIp("10.2.25.93").setHostName("uat0358"));
            slbRepository.add(slb);

            VirtualServer vs = new VirtualServer().setName("defaultSlbVs1").setSsl(false).setPort("80")
                    .addDomain(new Domain().setName("defaultSlbVs1.ctrip.com"));
            vs.getSlbIds().add(slb.getId());
            virtualServerRepository.add(vs);

            for (String path : existingPaths) {
                rGroupVsDao.insert(new RelGroupVsDo().setGroupId(10).setVsId(1).setPath(path).setPriority(1000));
            }
        }
    }

    private static String extractValue(String path) throws ValidationException {
        return DefaultGroupValidator.extractValue(path);
    }
}
