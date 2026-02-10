package com.foilen.smalltools.hash;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", HashSha256.hashFile(tmpFile));
    }

    @Test
    public void testHashString() {
        Assertions.assertEquals("a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e", HashSha256.hashString("Hello World"));
    }

}
