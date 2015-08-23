/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

public class FileToolsTest {

    private void assertFileContent(File tmpExpected, File tmpActual) {
        String expected = FileTools.getFileAsString(tmpExpected);
        String actual = FileTools.getFileAsString(tmpActual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAppendLineIfMissing_FileNotExists() throws Exception {
        // Files
        File tmpActual = File.createTempFile("junit", null);
        tmpActual.delete();
        File tmpExpected = File.createTempFile("junit", null);

        // File not exists
        FileTools.writeFile("hello world\n", tmpExpected);
        FileTools.appendLineIfMissing(tmpActual.getAbsolutePath(), "hello world");
        assertFileContent(tmpExpected, tmpActual);
    }

    @Test
    public void testAppendLineIfMissing_WithLineAtEnd() throws Exception {
        // Files
        File tmpActual = File.createTempFile("junit", null);
        File tmpExpected = File.createTempFile("junit", null);

        // File without line and with an empty ending line
        FileTools.writeFile("This is a nice project\nthat you are currently doing\nhello world\n", tmpActual);
        FileTools.writeFile("This is a nice project\nthat you are currently doing\nhello world\n", tmpExpected);
        FileTools.appendLineIfMissing(tmpActual.getAbsolutePath(), "hello world");
        assertFileContent(tmpExpected, tmpActual);
    }

    @Test
    public void testAppendLineIfMissing_WithLineInMiddle() throws Exception {
        // Files
        File tmpActual = File.createTempFile("junit", null);
        File tmpExpected = File.createTempFile("junit", null);

        // File with line in the middle
        FileTools.writeFile("This is a nice project\nhello world\nthat you are currently doing", tmpActual);
        FileTools.writeFile("This is a nice project\nhello world\nthat you are currently doing", tmpExpected);
        FileTools.appendLineIfMissing(tmpActual.getAbsolutePath(), "hello world");
        assertFileContent(tmpExpected, tmpActual);
    }

    @Test
    public void testAppendLineIfMissing_WithoutLine() throws Exception {
        // Files
        File tmpActual = File.createTempFile("junit", null);
        File tmpExpected = File.createTempFile("junit", null);

        // File without line
        FileTools.writeFile("This is a nice project\nthat you are currently doing", tmpActual);
        FileTools.writeFile("This is a nice project\nthat you are currently doing\nhello world\n", tmpExpected);
        FileTools.appendLineIfMissing(tmpActual.getAbsolutePath(), "hello world");
        assertFileContent(tmpExpected, tmpActual);
    }

    @Test
    public void testAppendLineIfMissing_WithoutLineWithEmptyEndingLine() throws Exception {
        // Files
        File tmpActual = File.createTempFile("junit", null);
        File tmpExpected = File.createTempFile("junit", null);

        // File without line and with an empty ending line
        FileTools.writeFile("This is a nice project\nthat you are currently doing\n", tmpActual);
        FileTools.writeFile("This is a nice project\nthat you are currently doing\nhello world\n", tmpExpected);
        FileTools.appendLineIfMissing(tmpActual.getAbsolutePath(), "hello world");
        assertFileContent(tmpExpected, tmpActual);
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
    public void testListFilesStartingWith() throws Exception {
        File directoryFile = Files.createTempDir();
        String directory = directoryFile.getAbsolutePath();

        FileTools.writeFile("#yes", directory + "/exact");
        FileTools.writeFile("#yes\nThis is interesting", directory + "/yesAndMore");
        FileTools.writeFile("Hello there", directory + "/random");
        FileTools.writeFile("#ye", directory + "/tooShort");
        FileTools.writeFile("#yeS", directory + "/near");

        // Check
        List<String> actual = FileTools.listFilesStartingWith(directory, "#yes");
        Assert.assertEquals(2, actual.size());
        int i = 0;
        Assert.assertEquals("exact", actual.get(i++));
        Assert.assertEquals("yesAndMore", actual.get(i++));
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

}
