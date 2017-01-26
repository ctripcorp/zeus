package com.ctrip.zeus.service.model;

import com.ctrip.zeus.AbstractServerTest;
import com.ctrip.zeus.dal.core.RGroupStatusDao;
import com.ctrip.zeus.dal.core.RGroupVsDao;
import com.ctrip.zeus.dal.core.RelGroupStatusDo;
import com.ctrip.zeus.dal.core.RelGroupVsDo;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.service.model.handler.GroupValidator;
import com.ctrip.zeus.service.model.grammar.PathUtils;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.dal.jdbc.DalException;

import javax.annotation.Resource;
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
    @Resource
    private RGroupStatusDao rGroupStatusDao;
    @Resource
    private PathValidator pathValidator;

    private final String standardSuffix = "($|/|\\?)";

    @Test
    public void testExtractUriFromRegexPath() throws ValidationException {
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
        Assert.assertEquals("abc($|/|\\?)", extractUri(normalValue1));
        Assert.assertEquals("abc", extractUri(normalValue2));
        Assert.assertEquals("abc", extractUri(normalValue3));
        Assert.assertEquals("abc", extractUri(normalValue4));
        Assert.assertEquals("abc", extractUri(normalValue5));
        Assert.assertEquals("abc", extractUri(normalValue6));
        Assert.assertEquals("abc($|/|\\?)", extractUri(normalValue7));
        Assert.assertEquals("abc", extractUri(creepyValue1));
        Assert.assertEquals("\\\"abc\\\"", extractUri(creepyValue2));
        Assert.assertEquals("members($|/|\\?)|membersite($|/|\\?)", extractUri(creepyValue3));
        Assert.assertEquals("/", extractUri(root1));
        Assert.assertEquals("/", extractUri(root2));
        Assert.assertEquals("/", extractUri(root3));
        Assert.assertEquals("/", extractUri(root4));
        Assert.assertEquals("/", extractUri(root5));
    }

    @Test
    public void testPathUtils() throws ValidationException {
        String s1 = "abcdefg";
        String s2 = "abc";
        String s3 = extractUri("ABCDefghij($|/|\\?)");
        String s4 = "我爱中国";
        String s5 = extractUri("我($|/|\\?)");
        String s6 = "我爱中国中国爱我";

        String s7 = extractUri("bcdef($|/|\\?)");
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

        Pattern p = Pattern.compile("^((\\w|-)+/?)(\\$)?");
        List<String> r;
        try {
            r = pathValidator.splitParallelPaths(s1, 1);
            Assert.assertEquals(2, r.size());
            Assert.assertEquals("Thingstodo-Order-OrderService$", r.get(0));
            Assert.assertEquals("Thingstodo-Order-OrderService/", r.get(1));

            Assert.assertTrue(p.matcher(r.get(0)).matches());
            Assert.assertTrue(p.matcher(r.get(1)).matches());
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

        try {
            r = pathValidator.splitParallelPaths(s2, 1);
            Assert.assertEquals(4, r.size());
            Assert.assertTrue(p.matcher(r.get(0)).matches());
            Assert.assertFalse(p.matcher(r.get(1)).matches());
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

        try {
            r = pathValidator.splitParallelPaths(s3, 1);
            Assert.assertEquals(8, r.size());
            Assert.assertEquals("restapi$", r.get(0));
            Assert.assertEquals("restapi/", r.get(1));
            Assert.assertEquals("html5$", r.get(2));
            Assert.assertEquals("html5/", r.get(3));
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

        try {
            r = pathValidator.splitParallelPaths(s4, 1);
            Assert.assertEquals(4, r.size());
            Assert.assertEquals("members$", r.get(0));
            Assert.assertEquals("members/", r.get(1));
            Assert.assertEquals("membersite$", r.get(2));
            Assert.assertEquals("membersite/", r.get(3));
        } catch (ValidationException e) {
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testAddExistingPath() {
        String samePath = "~* ^/linkservice($|/|\\?)";
        String partialPath = "~* ^/members($|/|\\?)";

        Group object = new Group().setId(100L);
        object.addGroupVirtualServer(new GroupVirtualServer().setPath(samePath).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        object.getGroupVirtualServers().get(0).setPriority(null).setPath(partialPath);
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }
    }

    @Test
    public void testAddNormalPath() {
        String path = "~* ^/normal($|/|\\?)";

        Group object = new Group().setId(100L);
        object.addGroupVirtualServer(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(object.getGroupVirtualServers().get(0).getPriority() == 1000);
            Assert.assertEquals(path, object.getGroupVirtualServers().get(0).getPath());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testAddOverlappingPath() {
        String path = "~* ^/linkservice/config($|/|\\?)";

        Group object = new Group().setId(100L);
        object.addGroupVirtualServer(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(object.getGroupVirtualServers().get(0).getPriority() == 1100);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            object.getGroupVirtualServers().get(0).setPriority(2000);
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(object.getGroupVirtualServers().get(0).getPriority() == 2000);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testUpdatePath() {
        String path = "~* ^/baike($|/|\\?)";

        Group object = new Group().setId(10L);
        object.addGroupVirtualServer(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertEquals("~* ^/baike($|/|\\?)", object.getGroupVirtualServers().get(0).getPath());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testAddUpperCasePath() {
        String path = "~* ^/BAIKE/CONFIG($|/|\\?)";

        Group object = new Group().setId(100L);
        object.addGroupVirtualServer(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(object.getGroupVirtualServers().get(0).getPriority() == 1100);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testAddRootPath() {
        String path = "~* /";

        Group object = new Group().setId(100L);
        object.addGroupVirtualServer(new GroupVirtualServer().setPath(path).setPriority(1000).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        object.getGroupVirtualServers().get(0).setPriority(null);
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(object.getGroupVirtualServers().get(0).getPriority() == -1000);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            RelGroupVsDo root = new RelGroupVsDo().setGroupId(10).setGroupVersion(1).setVsId(1).setPath("/").setPriority(-1000);
            rGroupVsDao.insert(root);

            try {
                object.getGroupVirtualServers().get(0).setPriority(null);
                groupModelValidator.validateGroupVirtualServers(object, false);
                Assert.assertTrue(object.getGroupVirtualServers().get(0).getPriority() == -900);
            } catch (Exception e) {
                Assert.assertTrue(false);
            }

            object.getGroupVirtualServers().get(0).setPath("/").setPriority(-1100);
            try {
                groupModelValidator.validateGroupVirtualServers(object, false);
                Assert.assertTrue(false);
            } catch (Exception e) {
                Assert.assertTrue(e instanceof ValidationException);
            }
            rGroupVsDao.delete(root);
        } catch (DalException e) {
            Assert.assertTrue(false);
        }

        object.getGroupVirtualServers().get(0).setPath("/").setPriority(null);
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(object.getGroupVirtualServers().get(0).getPriority() == -1100);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        testAddNormalPath();
    }

    @Test
    public void testUnconventionalPath() {
        String startWith = "^/CommentAdmin";
        String noStart = "baik";
        String noAlphabetic = "/123";
        String success = "success";
        String empty = "";
        String specialCase = "~* \"^/(thematic|topic)\"";
        String noMeaningSuffix = "~* \"^/members($|/|\\?)membersite($|/|\\?)\"";

        Group object = new Group().setId(100L);
        object.addGroupVirtualServer(new GroupVirtualServer().setVirtualServer(new VirtualServer().setId(1L)));

        try {
            object.getGroupVirtualServers().get(0).setPriority(null).setPath(success);
            groupModelValidator.validateGroupVirtualServers(object, false);
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            object.getGroupVirtualServers().get(0).setPriority(null).setPath(startWith);
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertEquals(900, object.getGroupVirtualServers().get(0).getPriority().intValue());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            object.getGroupVirtualServers().get(0).setPriority(null).setPath(noStart);
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertEquals(900, object.getGroupVirtualServers().get(0).getPriority().intValue());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            object.getGroupVirtualServers().get(0).setPath(noAlphabetic);
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertEquals(900, object.getGroupVirtualServers().get(0).getPriority().intValue());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            object.getGroupVirtualServers().get(0).setPath(empty);
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        try {
            object.getGroupVirtualServers().get(0).setPath(specialCase);
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertEquals(900, object.getGroupVirtualServers().get(0).getPriority().intValue());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }

        try {
            object.getGroupVirtualServers().get(0).setPath(noMeaningSuffix);
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        try {
            object.getGroupVirtualServers().get(0).setPriority(null).setPath(noMeaningSuffix);
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
        }
    }

    @Test
    public void testRegexExpression() {
        String path = "~* ^/ regex($|/|\\?)";

        Group object = new Group().setId(100L);
        object.addGroupVirtualServer(new GroupVirtualServer().setPath(path).setVirtualServer(new VirtualServer().setId(1L)));
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        object.getGroupVirtualServers().get(0).setPath("~= /regex");
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof ValidationException);
        }

        object.getGroupVirtualServers().get(0).setPath("=    /exact   ");
        try {
            groupModelValidator.validateGroupVirtualServers(object, false);
            Assert.assertEquals("= /exact", object.getGroupVirtualServers().get(0).getPath());
        } catch (Exception e) {
            Assert.assertTrue(false);
        }
    }

    @Before
    public void check() throws Exception {
        Slb slb = slbRepository.getById(1L);
        Set<String> existingPaths = Sets.newHashSet("~* ^/CommentAdmin/",
                "~* ^/baike($|/|\\?)",
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
                rGroupVsDao.insert(new RelGroupVsDo().setGroupId(10).setGroupVersion(1).setVsId(1).setPath(path).setPriority(1000));
            }
            rGroupStatusDao.insert(new RelGroupStatusDo().setGroupId(10).setOfflineVersion(1));
        }
    }

    private static String extractUri(String path) throws ValidationException {
        path = PathUtils.pathReformat(path);
        return PathUtils.extractUriIgnoresFirstDelimiter(path);
    }
}
