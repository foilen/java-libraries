/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.DirectoryTools;

public class DirectoryToolsTest {

    @Test
    public void testCleanupDots() {
        Assert.assertEquals("tmp/dir/file", DirectoryTools.cleanupDots("tmp/dir/file"));
        Assert.assertEquals("tmp/file", DirectoryTools.cleanupDots("tmp/dir/../file"));
        Assert.assertEquals("file", DirectoryTools.cleanupDots("tmp/dir/../../file"));
        Assert.assertEquals("file", DirectoryTools.cleanupDots("tmp/dir/../../../../file"));
        Assert.assertEquals("tmp/dir/file", DirectoryTools.cleanupDots("tmp/dir/./file"));
        Assert.assertEquals("tmp/file", DirectoryTools.cleanupDots("tmp/dir/.././file"));

        Assert.assertEquals("/tmp/dir/file", DirectoryTools.cleanupDots("/tmp/dir/file"));
        Assert.assertEquals("/tmp/file", DirectoryTools.cleanupDots("/tmp/dir/../file"));
        Assert.assertEquals("/file", DirectoryTools.cleanupDots("/tmp/dir/../../file"));
        Assert.assertEquals("/file", DirectoryTools.cleanupDots("/tmp/dir/../../../../file"));
        Assert.assertEquals("/tmp/dir/file", DirectoryTools.cleanupDots("/tmp/dir/./file"));
        Assert.assertEquals("/tmp/file", DirectoryTools.cleanupDots("/tmp/dir/.././file"));
    }

}
