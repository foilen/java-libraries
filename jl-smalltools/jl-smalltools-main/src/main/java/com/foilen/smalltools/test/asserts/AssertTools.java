/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.test.asserts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Assert;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.ResourceTools;

/**
 * Assertions.
 * 
 * <pre>
 * Dependencies:
 * testCompile 'com.fasterxml.jackson.core:jackson-databind:2.4.5'
 * testCompile 'org.yaml:snakeyaml:1.15'
 * testCompile 'junit:junit:4.12'
 * </pre>
 */
public final class AssertTools {

    public static void assertFileContent(File expectedFile, File actualFile) {
        // Check the file size
        Assert.assertEquals(expectedFile.length(), actualFile.length());

        // Check the files
        try {
            assertStreamContent(new FileInputStream(expectedFile), new FileInputStream(actualFile));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException("Issue opening the files", e);
        }

    }

    public static void assertFileContent(InputStream expectedStream, String actualFile) {
        // Check the files
        try {
            assertStreamContent(expectedStream, new FileInputStream(actualFile));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException("Issue opening the file", e);
        }
    }

    public static void assertIgnoreLineFeed(String expected, String actual) {
        Assert.assertEquals(expected.replaceAll("\r", ""), actual.replaceAll("\r", ""));
    }

    /**
     * Compare the expected object to the actual object (by their JSON dump).
     * 
     * @param expected
     *            the expected object to compare
     * @param actual
     *            the actual object to compare to
     */
    public static void assertJsonComparison(Object expected, Object actual) {
        String expectedJson = JsonTools.prettyPrint(expected);
        String actualJson = JsonTools.prettyPrint(actual);

        assertIgnoreLineFeed(expectedJson, actualJson);
    }

    /**
     * Load an expected object from a JSON resource file and compare it to the actual object (by their JSON dump).
     * 
     * @param expectedResource
     *            the filename of the resource
     * @param expectedContext
     *            the class in which the resource file is
     * @param actual
     *            the actual object to compare to
     */
    public static void assertJsonComparison(String expectedResource, Class<?> expectedContext, Object actual) {
        String expectedJson = ResourceTools.getResourceAsString(expectedResource, expectedContext);
        String actualJson = JsonTools.prettyPrint(actual);

        assertIgnoreLineFeed(expectedJson, actualJson);
    }

    public static void assertStreamContent(InputStream expectedStream, InputStream actualStream) {

        // Check the content
        long position = 0;
        try {

            byte[] expectedBytes = new byte[1024];
            int expectedLen;
            byte[] actualBytes = new byte[1024];
            int actualLen;

            do {
                expectedLen = expectedStream.read(expectedBytes);
                actualLen = actualStream.read(actualBytes);

                Assert.assertEquals(expectedLen, actualLen);
                Assert.assertArrayEquals("Position: " + position, expectedBytes, actualBytes);

                position += expectedLen;
            } while (expectedLen != -1);

        } catch (Exception e) {
            throw new SmallToolsException("Issue copying the stream", e);
        } finally {
            // Close the sources
            try {
                expectedStream.close();
            } catch (Exception e) {
            }
            try {
                actualStream.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Compare the expected object to the actual object (by their Yaml dump).
     * 
     * @param expected
     *            the expected object to compare
     * @param actual
     *            the actual object to compare to
     */
    public static void assertYamlComparison(Object expected, Object actual) {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setPrettyFlow(true);
        Yaml yaml = new Yaml(dumperOptions);
        String expectedYaml = yaml.dump(expected);
        String actualYaml = yaml.dump(actual);

        assertIgnoreLineFeed(expectedYaml, actualYaml);
    }

    /**
     * Load an expected object from a Yaml resource file and compare it to the actual object (by their Yaml dump).
     * 
     * @param expectedResource
     *            the filename of the resource
     * @param expectedContext
     *            the class in which the resource file is
     * @param actual
     *            the actual object to compare to
     */
    public static void assertYamlComparison(String expectedResource, Class<?> expectedContext, Object actual) {
        Yaml yaml = new Yaml();
        Object expected = yaml.load(expectedContext.getResourceAsStream(expectedResource));
        assertYamlComparison(expected, actual);
    }

    private AssertTools() {
    }

}