/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.IOException;

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
    public void testCompactPrint() {
        String expected = ResourceTools.getResourceAsString("JsonToolsTest-compactPrint-expected", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setA("hello");
        type.setB(10);
        String actual = JsonTools.compactPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testPrettyPrint() {
        String expected = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint-expected", this.getClass());
        expected = expected.replaceAll("\r", "");
        Type type = new Type();
        type.setA("hello");
        type.setB(10);
        String actual = JsonTools.prettyPrint(type).replaceAll("\r", "");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testReadFromFileFileClassOfT() {
        String content = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint-expected", this.getClass());
        FileTools.writeFile(content, tmpFile);
        Type type = JsonTools.readFromFile(tmpFile, Type.class);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromFileFileObject() {
        String content = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint-expected", this.getClass());
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
        String content = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint-expected", this.getClass());
        FileTools.writeFile(content, tmpFile);
        Type type = JsonTools.readFromFile(tmpFile.getAbsolutePath(), Type.class);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

    @Test
    public void testReadFromFileStringObject() {
        String content = ResourceTools.getResourceAsString("JsonToolsTest-prettyPrint-expected", this.getClass());
        FileTools.writeFile(content, tmpFile);

        Type type = new Type();
        type.setA("not");
        type.setB(1);
        JsonTools.readFromFile(tmpFile.getAbsolutePath(), type);

        Assert.assertEquals("hello", type.getA());
        Assert.assertEquals(10, type.getB());
    }

}
