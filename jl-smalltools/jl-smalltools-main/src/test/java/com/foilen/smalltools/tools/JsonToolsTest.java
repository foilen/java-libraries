/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;

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

    public static class TypeDeep {
        private String a;
        private int b;
        private TypeDeep c;
        private List<TypeDeep> d;
        private List<String> e;

        public String getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public TypeDeep getC() {
            return c;
        }

        public List<TypeDeep> getD() {
            return d;
        }

        public List<String> getE() {
            return e;
        }

        public TypeDeep setA(String a) {
            this.a = a;
            return this;
        }

        public TypeDeep setB(int b) {
            this.b = b;
            return this;
        }

        public TypeDeep setC(TypeDeep c) {
            this.c = c;
            return this;
        }

        public TypeDeep setD(List<TypeDeep> d) {
            this.d = d;
            return this;
        }

        public TypeDeep setE(List<String> e) {
            this.e = e;
            return this;
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

    @SuppressWarnings("unchecked")
    @Test
    public void testCloneAsSortedMap() {

        List<TypeDeep> list = new ArrayList<>();
        list.add(new TypeDeep().setA("first"));
        list.add(new TypeDeep().setA("second"));

        TypeDeep original = new TypeDeep().setA("Some text").setB(5).setC(new TypeDeep() //
                .setA("Some text depth 2").setB(10).setD(list).setC(new TypeDeep() //
                        .setA("Some text depth 3").setB(15).setE(Arrays.asList("abc", "def"))));

        SortedMap<String, Object> clone = JsonTools.cloneAsSortedMap(original);

        AssertTools.assertJsonComparisonWithoutNulls("JsonToolsTest-testCloneAsSortedMap-expected.json", getClass(), clone);

        // Assert
        Assert.assertTrue(clone.get("c") instanceof SortedMap);
        Map<String, Object> cDepth1 = (Map<String, Object>) clone.get("c");
        Assert.assertTrue(cDepth1.get("c") instanceof SortedMap);
        List<Object> clonedList = (List<Object>) cDepth1.get("d");
        Assert.assertTrue(clonedList.get(0) instanceof SortedMap);

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
