package com.ctrip.zeus.service.model;

import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.service.model.grammar.GrammarException;
import com.ctrip.zeus.service.model.grammar.PathParseHandler;
import com.ctrip.zeus.service.model.grammar.PathUtils;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by zhoumy on 2017/1/25.
 */
public class PathValidationV2Test {

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

    private static String extractUri(String path) throws ValidationException {
        path = PathUtils.pathReformat(path);
        return PathUtils.extractUriIgnoresFirstDelimiter(path);
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