/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.google.common.io.Files;

public class DirectoryToolsTest {

    @Test
    public void testDeleteFolder() throws IOException {

        File toDelete = Files.createTempDir();
        String toDeletePath = toDelete.getAbsolutePath();
        File keepSafe = Files.createTempDir();
        String keepSafePath = keepSafe.getAbsolutePath();

        // Create the directories, the files and the symlink
        Assert.assertTrue((DirectoryTools.createPath(toDeletePath + "/subOne/subTwo/subThree")));
        FileTools.writeFile("a", keepSafePath + "/aFile");
        java.nio.file.Files.createSymbolicLink( //
                new File(toDeletePath + "/subOne/NotFollow").toPath(), //
                keepSafe.toPath());
        FileTools.writeFile("a", toDeletePath + "/subOne/subTwo/hello");

        // Assert all is in place
        List<String> expected = new ArrayList<>();
        expected.add("subOne/");
        expected.add("subOne/NotFollow/");
        expected.add("subOne/NotFollow/aFile");
        expected.add("subOne/subTwo/");
        expected.add("subOne/subTwo/hello");
        expected.add("subOne/subTwo/subThree/");
        List<String> actual = DirectoryTools.list(toDelete, false);
        AssertTools.assertJsonComparison(expected, actual);

        expected = new ArrayList<>();
        expected.add("aFile");
        actual = DirectoryTools.list(keepSafe, false);
        AssertTools.assertJsonComparison(expected, actual);

        // Delete
        DirectoryTools.deleteFolder(toDelete);

        // Assert all deleted, but the keepSafe
        Assert.assertFalse(toDelete.exists());
        AssertTools.assertJsonComparison(expected, actual);

        expected = new ArrayList<>();
        expected.add("aFile");
        actual = DirectoryTools.list(keepSafe, false);
        AssertTools.assertJsonComparison(expected, actual);
    }

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
