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

    private AssertTools() {
    }

}
