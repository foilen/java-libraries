package com.foilen.smalltools.hash;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertEquals("b10a8db164e0754105b7a99be72e3fe5", HashMd5sum.hashFile(tmpFile));
    }

    @Test
    public void testHashString() {
        Assertions.assertEquals("b10a8db164e0754105b7a99be72e3fe5", HashMd5sum.hashString("Hello World"));
    }

}
