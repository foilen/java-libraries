/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

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
 * Tests for {@link HashSha256}.
 */
public class HashSha256Test {

    @Test
    public void testHashFile() throws IOException {
        // Copy the resource in a temp file
        File tmpFile = File.createTempFile("junits", null);
        ResourceTools.copyToFile("HashUtils.txt", HashSha256Test.class, tmpFile);

        // Hash it
        Assert.assertEquals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", HashSha256.hashFile(tmpFile));
    }

    @Test
    public void testHashString() {
        Assert.assertEquals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", HashSha256.hashString("Hello World"));
    }

}
