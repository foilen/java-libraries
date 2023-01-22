/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class YamlToolsTest {

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

        Type clone = YamlTools.clone(original);

        Assert.assertTrue(original != clone);
        Assert.assertEquals("Some text", clone.getA());
        Assert.assertEquals(5, clone.getB());
    }

    @Test
    public void testClone_null() {
        Type clone = YamlTools.clone(null);
        Assert.assertNull(clone);
    }

    @Test
    public void testCompactPrint_filled() {
        String expected = ResourceTools.getResourceAsString("YamlToolsTest-compactPrint_filled-expected.yaml", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setA("hello");
        type.setB(10);
        String actual = YamlTools.compactPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCompactPrint_withNull() {
        String expected = ResourceTools.getResourceAsString("YamlToolsTest-compactPrint_withNull-expected.yaml", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setB(10);
        String actual = YamlTools.compactPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPrettyPrint_filled() {
        String expected = ResourceTools.getResourceAsString("YamlToolsTest-prettyPrint_filled-expected.yaml", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setA("hello");
        type.setB(10);
        String actual = YamlTools.prettyPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPrettyPrint_withNull() {
        String expected = ResourceTools.getResourceAsString("YamlToolsTest-prettyPrint_withNull-expected.yaml", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setB(10);
        String actual = YamlTools.prettyPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReadFromFileFileClassOfT() {
        String content = ResourceTools.getResourceAsString("YamlToolsTest-prettyPrint_filled-expected.yaml", this.getClass());
        FileTools.writeFile(content, tmpFile);
        Type type = YamlTools.readFromFile(tmpFile, Type.class);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromFileFileObject() {
        String content = ResourceTools.getResourceAsString("YamlToolsTest-prettyPrint_filled-expected.yaml", this.getClass());
        FileTools.writeFile(content, tmpFile);

        Type type = new Type();
        type.setA("not");
        type.setB(1);
        YamlTools.readFromFile(tmpFile, type);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromFileStringClassOfT() {
        String content = ResourceTools.getResourceAsString("YamlToolsTest-prettyPrint_filled-expected.yaml", this.getClass());
        FileTools.writeFile(content, tmpFile);
        Type type = YamlTools.readFromFile(tmpFile.getAbsolutePath(), Type.class);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromFileStringObject() {
        String content = ResourceTools.getResourceAsString("YamlToolsTest-prettyPrint_filled-expected.yaml", this.getClass());
        FileTools.writeFile(content, tmpFile);

        Type type = new Type();
        type.setA("not");
        type.setB(1);
        YamlTools.readFromFile(tmpFile.getAbsolutePath(), type);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testWriteToStream() throws Exception {

        OutputStream stream = new FileOutputStream(tmpFile);
        YamlTools.writeToStream(stream, "First");
        YamlTools.writeToStream(stream, "Second");
        stream.close();

        String actual = FileTools.getFileAsString(tmpFile);
        Assert.assertEquals("First\nSecond\n", actual);
    }

}
