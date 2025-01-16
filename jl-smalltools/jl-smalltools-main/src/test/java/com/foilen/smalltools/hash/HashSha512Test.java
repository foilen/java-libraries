/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.hash;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tools.ResourceTools;

/**
 * Tests for {@link HashSha512}.
 */
public class HashSha512Test {

    @Test
    public void testHashFile() throws IOException {
        // Copy the resource in a temp file
        File tmpFile = File.createTempFile("junits", null);
        ResourceTools.copyToFile("HashUtils.txt", HashSha512Test.class, tmpFile);

        // Hash it
        Assert.assertEquals("2c74fd17edafd80e8447b0d46741ee243b7eb74dd2149a0ab1b9246fb30382f27e853d8585719e0e67cbda0daa8f51671064615d645ae27acb15bfb1447f459b", HashSha512.hashFile(tmpFile));
    }

    @Test
    public void testHashString() {
        Assert.assertEquals("2c74fd17edafd80e8447b0d46741ee243b7eb74dd2149a0ab1b9246fb30382f27e853d8585719e0e67cbda0daa8f51671064615d645ae27acb15bfb1447f459b", HashSha512.hashString("Hello World"));
    }

}
