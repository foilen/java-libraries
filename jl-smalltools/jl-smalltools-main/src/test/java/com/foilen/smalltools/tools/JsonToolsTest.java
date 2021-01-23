/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JsonToolsTest {

    public static class Type {
        private String a;
        private int b;

        public String getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public void setA(String a) {
            this.a = a;
        }

        public void setB(int b) {
            this.b = b;
        }

    }

    private File tmpFile;

    @Before
    public void before() throws IOException {
        tmpFile = File.createTempFile("junit", null);
    }

    @Test
    public void testClone() {

        Type original = new Type();
        original.setA("Some text");
        original.setB(5);

        Type clone = JsonTools.clone(original);

        Assert.assertTrue(original != clone);
        Assert.assertEquals("Some text", clone.getA());
        Assert.assertEquals(5, clone.getB());
    }

    @Test
    public void testClone_null() {
        Type clone = JsonTools.clone(null);
        Assert.assertNull(clone);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testClone_otherType() {

        Type original = new Type();
        original.setA("Some text");
        original.setB(5);

        Object clone = JsonTools.clone(original, Object.class);

        Assert.assertTrue(original != clone);
        Assert.assertTrue(clone instanceof Map);
        Map<?, ?> cloneMap = (Map) clone;

        Assert.assertEquals("Some text", cloneMap.get("a"));
        Assert.assertEquals(5, cloneMap.get("b"));
    }

    @Test
    public void testCompactPrint_filled() {
        String expected = ResourceTools.getResourceAsString("JsonToolsTest-compactPrint_filled-expected.json", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setA("hello");
        type.setB(10);
        String actual = JsonTools.compactPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCompactPrint_withNull() {
        String expected = ResourceTools.getResourceAsString("JsonToolsTest-compactPrint_withNull-expected.json", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setB(10);
        String actual = JsonTools.compactPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCompactPrint_withoutNull() {
        String expected = ResourceTools.getResourceAsString("JsonToolsTest-compactPrint_withoutNull-expected.json", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setB(10);
        String actual = JsonTools.compactPrintWithoutNulls(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPrettyPrint_filled() {
        String expected = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint_filled-expected.json", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setA("hello");
        type.setB(10);
        String actual = JsonTools.prettyPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPrettyPrint_withNull() {
        String expected = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint_withNull-expected.json", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setB(10);
        String actual = JsonTools.prettyPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPrettyPrint_withoutNull() {
        String expected = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint_withoutNull-expected.json", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setB(10);
        String actual = JsonTools.prettyPrintWithoutNulls(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReadFromFileFileClassOfT() {
        String content = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint_filled-expected.json", this.getClass());
        FileTools.writeFile(content, tmpFile);
        Type type = JsonTools.readFromFile(tmpFile, Type.class);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromFileFileObject() {
        String content = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint_filled-expected.json", this.getClass());
        FileTools.writeFile(content, tmpFile);

        Type type = new Type();
        type.setA("not");
        type.setB(1);
        JsonTools.readFromFile(tmpFile, type);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromFileStringClassOfT() {
        String content = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint_filled-expected.json", this.getClass());
        FileTools.writeFile(content, tmpFile);
        Type type = JsonTools.readFromFile(tmpFile.getAbsolutePath(), Type.class);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromFileStringObject() {
        String content = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint_filled-expected.json", this.getClass());
        FileTools.writeFile(content, tmpFile);

        Type type = new Type();
        type.setA("not");
        type.setB(1);
        JsonTools.readFromFile(tmpFile.getAbsolutePath(), type);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromResourceAsList() {
        List<Type> actual = JsonTools.readFromResourceAsList("JsonToolsTest-testReadFromResourceAsList.json", Type.class, this.getClass());

        Assert.assertEquals(2, actual.size());

        Assert.assertEquals("aa", actual.get(0).getA());
        Assert.assertEquals(12, actual.get(0).getB());

        Assert.assertEquals("bb", actual.get(1).getA());
        Assert.assertEquals(34, actual.get(1).getB());
    }

    @Test
    public void testReadFromStringAsList() {
        String json = ResourceTools.getResourceAsString("JsonToolsTest-testReadFromResourceAsList.json", this.getClass());
        List<Type> actual = JsonTools.readFromStringAsList(json, Type.class);

        Assert.assertEquals(2, actual.size());

        Assert.assertEquals("aa", actual.get(0).getA());
        Assert.assertEquals(12, actual.get(0).getB());

        Assert.assertEquals("bb", actual.get(1).getA());
        Assert.assertEquals(34, actual.get(1).getB());
    }

    @Test
    public void testWriteToStream() throws Exception {

        OutputStream stream = new FileOutputStream(tmpFile);
        JsonTools.writeToStream(stream, "First");
        JsonTools.writeToStream(stream, "Second");
        stream.close();

        String actual = FileTools.getFileAsString(tmpFile);
        Assert.assertEquals("\"First\"\"Second\"", actual);
    }

}
