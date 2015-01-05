/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.FileTools;

public class FileToolsTest {

    @Test
    public void testIsWindowsStartPath() {
        Assert.assertTrue(FileTools.isWindowsStartPath("c:\\windows\\notepad.exe"));
        Assert.assertTrue(FileTools.isWindowsStartPath("C:\\windows\\notepad.exe"));
        Assert.assertTrue(FileTools.isWindowsStartPath("c:/windows/note:pad.exe"));
        Assert.assertFalse(FileTools.isWindowsStartPath("/windows/note:pad.exe"));
        Assert.assertFalse(FileTools.isWindowsStartPath("\\windows\\note:pad.exe"));
        Assert.assertFalse(FileTools.isWindowsStartPath("c\\windows\\notepad.exe"));
    }

    @Test
    public void testReadFileLinesIteration() throws IOException {
        File tmpFile = File.createTempFile("junit", null);
        String content = "This is the first line\nAnd the second one\nA last one";
        Assert.assertTrue(FileTools.writeFile(content, tmpFile));

        String[] parts = content.split("\n");
        int count = 0;
        for (String nextLine : FileTools.readFileLinesIteration(tmpFile.getAbsolutePath())) {
            Assert.assertEquals(parts[count++], nextLine);
        }
    }

    @Test
    public void testGetAbsolutePathAbsolute() {
        // Unix
        String workingDirectory = "/tmp/";
        String destinationPath = "/home/junit/file";
        String actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals(destinationPath, actual);

        // Windows lower case
        workingDirectory = "c:\\tmp\\";
        destinationPath = "c:\\windows\\notepad.exe";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals(destinationPath, actual);

        // Windows upper case
        workingDirectory = "C:\\tmp\\";
        destinationPath = "C:\\windows\\notepad.exe";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals(destinationPath, actual);
    }

    @Test
    public void testGetAbsolutePathRelative() {
        // Unix forward
        String workingDirectory = "/tmp/";
        String destinationPath = "junit/file";
        String actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("/tmp/junit/file", actual);

        // Unix backward one
        workingDirectory = "/tmp/junit/";
        destinationPath = "../junit2/file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("/tmp/junit2/file", actual);

        // Unix backward two
        workingDirectory = "/tmp/dir1/dir2/";
        destinationPath = "../../junit2/file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("/tmp/junit2/file", actual);

        // Unix backward a lot
        workingDirectory = "/tmp/dir1/";
        destinationPath = "../../../../../junit2/file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("/junit2/file", actual);

        // Windows forward
        workingDirectory = "c:\\tmp\\";
        destinationPath = "junit\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("c:\\tmp\\junit\\file", actual);

        // Windows backward one
        workingDirectory = "c:\\tmp\\junit\\";
        destinationPath = "..\\junit2\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("c:\\tmp\\junit2\\file", actual);

        // Windows backward two
        workingDirectory = "c:\\tmp\\dir1\\dir2\\";
        destinationPath = "..\\..\\junit2\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("c:\\tmp\\junit2\\file", actual);

        // Windows backward a lot
        workingDirectory = "c:\\tmp\\dir1\\";
        destinationPath = "..\\..\\..\\..\\..\\junit2\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("c:\\junit2\\file", actual);

        // Unix forward without ending separator
        workingDirectory = "/tmp";
        destinationPath = "junit/file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("/tmp/junit/file", actual);

        // Windows forward without ending separator
        workingDirectory = "c:\\tmp";
        destinationPath = "junit\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assert.assertEquals("c:\\tmp\\junit\\file", actual);
    }

}
