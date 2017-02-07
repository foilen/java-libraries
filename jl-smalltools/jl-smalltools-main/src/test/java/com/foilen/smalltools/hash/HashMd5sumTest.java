/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

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
 * Tests for {@link HashMd5sum}.
 */
public class HashMd5sumTest {

    @Test
    public void testHashFile() throws IOException {
        // Copy the resource in a temp file
        File tmpFile = File.createTempFile("junits", null);
        ResourceTools.copyToFile("HashUtils.txt", HashSha256Test.class, tmpFile);

        // Hash it
        Assert.assertEquals("b10a8db164e0754105b7a99be72e3fe5", HashMd5sum.hashFile(tmpFile));
    }

    @Test
    public void testHashString() {
        Assert.assertEquals("b10a8db164e0754105b7a99be72e3fe5", HashMd5sum.hashString("Hello World"));
    }

}
