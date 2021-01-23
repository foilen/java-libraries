/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

public class DirectoryToolsTest {

    private void createFile(File rootDir, String path) {
        File file = new File(rootDir.getAbsolutePath() + File.separatorChar + path);
        DirectoryTools.createPathToFile(file.getAbsolutePath());
        FileTools.writeFile("", file);
    }

    private void createFile(File rootDir, String path, Date lastModified) {
        File file = new File(rootDir.getAbsolutePath() + File.separatorChar + path);
        DirectoryTools.createPathToFile(file.getAbsolutePath());
        FileTools.writeFile("", file);
        file.setLastModified(lastModified.getTime());
    }

    private void createFolder(File rootDir, String path) {
        DirectoryTools.createPath(rootDir.getAbsolutePath() + File.separatorChar + path);
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
    public void testDeleteEmptySubFolders() {

        // Create files
        File rootDir = Files.createTempDir();
        createFile(rootDir, "a/a/one.txt");
        createFile(rootDir, "b/b/b/one.txt");
        createFolder(rootDir, "b/b/b/e/e1");
        createFolder(rootDir, "b/b/b/e/e2/e");
        createFolder(rootDir, "b/b/b/e/e2");
        createFolder(rootDir, "e");

        // Execute
        int removed = DirectoryTools.deleteEmptySubFolders(rootDir.getAbsolutePath());

        // Assert
        Assert.assertEquals(5, removed);

        String actual = Joiner.on('\n').join(DirectoryTools.listFilesAndFoldersRecursively(rootDir, false));
        AssertTools.assertIgnoreLineFeed(Joiner.on('\n').join(Arrays.asList( //
                "a/", //
                "a/a/", //
                "a/a/one.txt", //
                "b/", //
                "b/b/", //
                "b/b/b/", //
                "b/b/b/one.txt" //
        )), //
                actual);
    }

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
        List<String> actual = DirectoryTools.listFilesAndFoldersRecursively(toDelete, false);
        AssertTools.assertJsonComparison(expected, actual);

        expected = new ArrayList<>();
        expected.add("aFile");
        actual = DirectoryTools.listFilesAndFoldersRecursively(keepSafe, false);
        AssertTools.assertJsonComparison(expected, actual);

        // Delete
        DirectoryTools.deleteFolder(toDelete);

        // Assert all deleted, but the keepSafe
        Assert.assertFalse(toDelete.exists());
        AssertTools.assertJsonComparison(expected, actual);

        expected = new ArrayList<>();
        expected.add("aFile");
        actual = DirectoryTools.listFilesAndFoldersRecursively(keepSafe, false);
        AssertTools.assertJsonComparison(expected, actual);
    }

    @Test
    public void testDeleteOlderFilesInDirectory() {

        // Create files
        File rootDir = Files.createTempDir();
        for (int i = 0; i < 20; ++i) {
            createFile(rootDir, "a/a/" + i + ".txt", DateTools.addDate(Calendar.HOUR, -i));
        }
        for (int i = 0; i < 40; ++i) {
            createFile(rootDir, "b/" + i + ".txt", DateTools.addDate(Calendar.HOUR, -i));
        }
        for (int i = 0; i < 50; ++i) {
            createFile(rootDir, "a/c/d/" + i + ".txt", DateTools.addDate(Calendar.HOUR, -i));
        }

        // Execute
        int removed = DirectoryTools.deleteOlderFilesInDirectory(rootDir.getAbsolutePath(), DateTools.addDate(Calendar.MINUTE, -130));

        // Assert
        Assert.assertEquals(110 - 9, removed);

        String actual = Joiner.on('\n').join(DirectoryTools.listFilesAndFoldersRecursively(rootDir, false));
        AssertTools.assertIgnoreLineFeed(Joiner.on('\n').join(Arrays.asList( //
                "a/", //
                "a/a/", //
                "a/a/0.txt", //
                "a/a/1.txt", //
                "a/a/2.txt", //
                "a/c/", //
                "a/c/d/", //
                "a/c/d/0.txt", //
                "a/c/d/1.txt", //
                "a/c/d/2.txt", //
                "b/", //
                "b/0.txt", //
                "b/1.txt", //
                "b/2.txt" //
        )), //
                actual);
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
