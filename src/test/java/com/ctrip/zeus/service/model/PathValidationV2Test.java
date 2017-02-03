package com.ctrip.zeus.service.model;

import com.ctrip.zeus.service.model.common.LocationEntry;
import com.ctrip.zeus.service.model.common.MetaType;
import com.ctrip.zeus.service.model.common.ValidationContext;
import com.ctrip.zeus.service.model.grammar.GrammarException;
import com.ctrip.zeus.service.model.grammar.PathParseHandler;
import com.ctrip.zeus.service.model.grammar.PathUtils;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by zhoumy on 2017/1/25.
 */
public class PathValidationV2Test {

    @Test
    public void testModifiablePriority() {
        PathValidator pathValidator = new PathValidator();
        List<LocationEntry> entries = new ArrayList<>();
        ValidationContext context = new ValidationContext();

        AtomicLong tick = new AtomicLong(1L);
        entries.add(generateGroupLocation(tick, "~* /abc", 1000));
        entries.add(generateGroupLocation(tick, "~* /abcde", 1100));
        entries.add(generateGroupLocation(tick, "~* /acc", 1000));
        entries.add(generateGroupLocation(tick, "~* /aff", 1000));
        LocationEntry target1 = generateGroupLocation(tick, "~* /abcd", null);
        LocationEntry target2 = generateGroupLocation(tick, "~* /afff", null);
        LocationEntry target3 = generateGroupLocation(tick, "~* /ac", null);
        LocationEntry target4 = generateGroupLocation(tick, "~* /ad", null);
        entries.add(target1);
        entries.add(target2);
        entries.add(target3);
        entries.add(target4);
        pathValidator.checkOverlapRestricition(entries, context);
        Assert.assertTrue(context.shouldProceed());
        Assert.assertEquals(1050, target1.getPriority().intValue());
        Assert.assertEquals(1100, target2.getPriority().intValue());
        Assert.assertEquals(900, target3.getPriority().intValue());
        Assert.assertEquals(1000, target4.getPriority().intValue());
    }

    @Test
    public void testUnmodifiablePriority_case1() {
        PathValidator pathValidator = new PathValidator();
        List<LocationEntry> entries = new ArrayList<>();
        ValidationContext context = new ValidationContext();

        AtomicLong tick = new AtomicLong(1L);
        entries.add(generateGroupLocation(tick, "~* /abc", 1000));
        entries.add(generateGroupLocation(tick, "~* /abcde", 1001));
        entries.add(generateGroupLocation(tick, "~* /", 800));
        entries.add(generateGroupLocation(tick, "~* /acc", 801));
        LocationEntry target1 = generateGroupLocation(tick, "~* /abcd", null);
        LocationEntry target2 = generateGroupLocation(tick, "~* /ac", null);
        entries.add(target1);
        entries.add(target2);
        pathValidator.checkOverlapRestricition(entries, context);
        Assert.assertFalse(context.shouldProceed());
        Assert.assertArrayEquals(new Long[]{target1.getEntryId(), target2.getEntryId()}, context.getErrorGroups().toArray(new Long[]{}));

        System.out.println("**************** ERROR ****************");
        for (Map.Entry<String, String> e : context.getErrors().entrySet()) {
            System.out.println(e.getKey() + ":" + e.getValue());
        }
    }

    @Test
    public void testUnmodifiablePriority_case2() {
        PathValidator pathValidator = new PathValidator();
        List<LocationEntry> entries = new ArrayList<>();
        ValidationContext context = new ValidationContext();

        AtomicLong tick = new AtomicLong(1L);
        entries.add(generateGroupLocation(tick, "~* /abc", 1000));
        entries.add(generateGroupLocation(tick, "~* /abcde", 800));
        pathValidator.checkOverlapRestricition(entries, context);
        Assert.assertFalse(context.shouldProceed());
        Assert.assertEquals(2, context.getErrorGroups().size());

        System.out.println("**************** ERROR ****************");
        for (Map.Entry<String, String> e : context.getErrors().entrySet()) {
            System.out.println(e.getKey() + ":" + e.getValue());
        }
    }

    @Test
    public void testFindAllIssueEntries() {
        PathValidator pathValidator = new PathValidator();
        List<LocationEntry> entries = new ArrayList<>();
        ValidationContext context = new ValidationContext();

        AtomicLong tick = new AtomicLong(1L);
        entries.add(generateGroupLocation(tick, "~* /a", 700));
        entries.add(generateGroupLocation(tick, "~* /ab", 800));
        entries.add(generateGroupLocation(tick, "~* /abc", 900));
        entries.add(generateGroupLocation(tick, "~* /abcd", 1000));
        entries.add(generateGroupLocation(tick, "~* /abce", 1000));
        entries.add(generateGroupLocation(tick, "~* /abcf", 1000));
        entries.add(generateGroupLocation(tick, "~* /abcdefg", 1002));
        LocationEntry target1 = generateGroupLocation(tick, "~* /abcde", null);
        LocationEntry target2 = generateGroupLocation(tick, "~* /abcdef", null);
        entries.add(target1);
        entries.add(target2);
        pathValidator.checkOverlapRestricition(entries, context);
        Assert.assertFalse(context.shouldProceed());
        Assert.assertArrayEquals(new Long[]{target1.getEntryId(), target2.getEntryId()}, context.getErrorGroups().toArray(new Long[]{}));

        System.out.println("**************** ERROR ****************");
        for (Map.Entry<String, String> e : context.getErrors().entrySet()) {
            System.out.println(e.getKey() + ":" + e.getValue());
        }
    }

    private LocationEntry generateGroupLocation(AtomicLong tick, String path, Integer priority) {
        return new LocationEntry().setEntryId(tick.getAndIncrement()).setEntryType(MetaType.GROUP).setPriority(priority).setPath(path).setVsId(1L);
    }

    @Test
    public void testExtractUriFromRegexPath() throws Exception {
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

    private static String extractUri(String path) throws Exception {
        PathUtils.pathReformat(path);
        return new String(PathParseHandler.extractUriIgnoresFirstDelimiter(path.toCharArray()));
    }

    @Test
    public void testResolveRegexPattern() throws GrammarException {
        PathParseHandler pph = new PathParseHandler();
        Assert.assertArrayEquals(new String[]{"abc"}, pph.parse("abc"));
        Assert.assertArrayEquals(new String[]{"/"}, pph.parse("/"));
        Assert.assertArrayEquals(new String[]{"a"}, pph.parse("((((a))))"));
        Assert.assertArrayEquals(new String[]{"abbefg", "abcefg", "abbeff", "abceff"}, pph.parse("ab(b|c)ef(g|f)"));
        Assert.assertArrayEquals(new String[]{"rest", "html", "weba", "webc"}, pph.parse("rest|html|web(a|c)"));
        Assert.assertArrayEquals(new String[]{"ababct/d", "abdeft/d", "abat/d", "abct/d"}, pph.parse("ab((abc|def)|(a|c))t/d"));
        Assert.assertArrayEquals(new String[]{"ababcat/d", "abdefat/d", "ababcct/d", "abdefct/d"}, pph.parse("ab((abc|def)(a|c))t/d"));
        Assert.assertArrayEquals(new String[]{"acd", "bcd", "html"}, pph.parse("(a|b)cd|html"));
        Assert.assertArrayEquals(new String[]{"abc", "ef"}, pph.parse("abc|ef"));
        Assert.assertArrayEquals(new String[]{"\\(\\(a"}, pph.parse("((\\(\\(a))"));
        Assert.assertArrayEquals(new String[]{"abccc", "showjournal-testcc", "bacc", "btacc"}, pph.parse("(abc|show(journal)-test|b(a|ta))cc"));
        Assert.assertArrayEquals(new String[]{"\\\"abc\\\""}, pph.parse("\\\"abc\\\""));
        Assert.assertArrayEquals(new String[]{"members$", "members/", "membersite$", "membersite/"}, pph.parse("members($|/|\\?)|membersite($|/|\\?)"));
    }

    @Test
    public void testErrorRegexPattern() {
        PathParseHandler pph = new PathParseHandler();
        List<String> err = Lists.newArrayList("((a", "(a))", "a?", "(a+)*aa");
        System.out.printf("%-10s| %s\n", "input", "error");
        for (String s : err) {
            try {
                pph.parse(s);
                Assert.assertTrue(false);
            } catch (GrammarException e) {
                System.out.printf("%-10s| %s\n", s, e.getMessage());
            }
        }
    }
}