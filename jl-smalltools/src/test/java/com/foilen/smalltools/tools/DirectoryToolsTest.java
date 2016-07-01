/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

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

    @Test
    public void testListFilesStartingWith() throws Exception {
        File directoryFile = Files.createTempDir();
        String directory = directoryFile.getAbsolutePath();

        FileTools.writeFile("#yes", directory + "/exact");
        FileTools.writeFile("#yes\nThis is interesting", directory + "/yesAndMore");
        FileTools.writeFile("Hello there", directory + "/random");
        FileTools.writeFile("#ye", directory + "/tooShort");
        FileTools.writeFile("#yeS", directory + "/near");

        // Check
        List<String> actual = DirectoryTools.listFilesStartingWith(directory, "#yes");
        Assert.assertEquals(2, actual.size());
        int i = 0;
        Assert.assertEquals("exact", actual.get(i++));
        Assert.assertEquals("yesAndMore", actual.get(i++));
    }

}
