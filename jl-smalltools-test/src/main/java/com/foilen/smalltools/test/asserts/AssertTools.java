/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.test.asserts;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.foilen.smalltools.test.exception.StTestException;

/**
 * Assertions.
 */
public final class AssertTools {

    public static void assertFileContent(File expectedFile, File actualFile) {
        // Check the file size
        Assert.assertEquals(expectedFile.length(), actualFile.length());

        // Check the content
        InputStream expectedIS = null;
        InputStream actualIS = null;
        long position = 0;
        try {
            expectedIS = new FileInputStream(expectedFile);
            actualIS = new FileInputStream(actualFile);

            byte[] expectedBytes = new byte[1024];
            int expectedLen;
            byte[] actualBytes = new byte[1024];
            int actualLen;

            do {
                expectedLen = expectedIS.read(expectedBytes);
                actualLen = actualIS.read(actualBytes);

                Assert.assertEquals(expectedLen, actualLen);
                Assert.assertArrayEquals("Position: " + position, expectedBytes, actualBytes);

                position += expectedLen;
            } while (expectedLen != -1);

        } catch (Exception e) {
            throw new StTestException("Issue copying the stream", e);
        } finally {

            // Close the sources
            try {
                expectedIS.close();
            } catch (Exception e) {
            }
            try {
                actualIS.close();
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

        Assert.assertEquals(expectedYaml, actualYaml);
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
