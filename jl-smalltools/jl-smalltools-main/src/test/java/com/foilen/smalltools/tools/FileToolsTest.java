/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.tuple.Tuple2;

public class FileToolsTest {

    private void assertFileContent(File tmpExpected, File tmpActual) {
        String expected = FileTools.getFileAsString(tmpExpected);
        String actual = FileTools.getFileAsString(tmpActual);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAppendLineAndReadFileLinesStream() throws Exception {
        File tmpFile = File.createTempFile("junit", null);
        FileTools.appendLine(tmpFile, "hello world");
        FileTools.appendLine(tmpFile, "aligator");
        FileTools.appendLine(tmpFile.getAbsolutePath(), "yep");

        List<String> lines = FileTools.readFileLinesStream(tmpFile).collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList("hello world", "aligator", "yep"), lines);

        lines = FileTools.readFileLinesStream(tmpFile.getAbsolutePath()).collect(Collectors.toList());
        Assert.assertEquals(Arrays.asList("hello world", "aligator", "yep"), lines);
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
    public void testClearAndDeleteAndGetFileAsBytes() throws Exception {
        File tmpFile = File.createTempFile("junit", null);
        tmpFile.delete();

        // Does not exists
        Assert.assertFalse(FileTools.exists(tmpFile.getAbsolutePath()));

        // Create empty
        FileTools.clearFile(tmpFile);
        Assert.assertTrue(FileTools.exists(tmpFile.getAbsolutePath()));
        Assert.assertEquals(0, FileTools.getFileAsBytes(tmpFile).length);

        // Put some things in it and clear it
        FileTools.writeFile("hello", tmpFile);
        Assert.assertTrue(FileTools.exists(tmpFile.getAbsolutePath()));
        Assert.assertNotEquals(0, FileTools.getFileAsBytes(tmpFile).length);

        FileTools.clearFile(tmpFile.getAbsolutePath());
        Assert.assertTrue(FileTools.exists(tmpFile.getAbsolutePath()));
        Assert.assertEquals(0, FileTools.getFileAsBytes(tmpFile).length);

        // Delete
        FileTools.deleteFile(tmpFile.getAbsolutePath());
        Assert.assertFalse(FileTools.exists(tmpFile.getAbsolutePath()));
    }

    @Test
    public void testCreateStagingFile() throws Exception {
        // Prepare files
        File stagingFile = File.createTempFile("junit", null);
        File finalFile = File.createTempFile("junit", null);
        stagingFile.delete();
        finalFile.delete();

        // Create
        OutputStream outputStream = FileTools.createStagingFile(stagingFile, finalFile);
        Assert.assertTrue(stagingFile.exists());
        Assert.assertFalse(finalFile.exists());

        // Put some data
        outputStream.write("yay".getBytes(CharsetTools.UTF_8));
        Assert.assertTrue(stagingFile.exists());
        Assert.assertFalse(finalFile.exists());

        // Close and check final
        outputStream.close();
        Assert.assertFalse(stagingFile.exists());
        Assert.assertTrue(finalFile.exists());

        Assert.assertEquals("yay", FileTools.getFileAsString(finalFile));
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
    public void testGetExtension() {
        Assert.assertEquals("js", FileTools.getExtension("all.js"));
        Assert.assertNull(FileTools.getExtension("all-test"));
        Assert.assertEquals("", FileTools.getExtension("all."));

        Assert.assertEquals("js", FileTools.getExtension("/tmp.test/all.js"));
        Assert.assertNull(FileTools.getExtension("/tmp.test/all-test"));
        Assert.assertEquals("", FileTools.getExtension("/tmp.test/all."));
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
    public void testPermissions() throws IOException {
        File tmpFile = File.createTempFile("junit", null);

        // Test a file
        FileTools.changePermissions(tmpFile.getAbsolutePath(), false, "755");
        Assert.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath()));

        FileTools.changePermissions(tmpFile.getAbsolutePath(), false, "644");
        Assert.assertEquals("644", FileTools.getPermissions(tmpFile.getAbsolutePath()));

        // Test a folder recursive
        tmpFile.delete();
        Assert.assertTrue(DirectoryTools.createPath(tmpFile.getAbsolutePath() + "/sub"));
        FileTools.writeFile("hello", tmpFile.getAbsolutePath() + "/sub/aFile");
        FileTools.writeFile("hello", tmpFile.getAbsolutePath() + "/aFile");
        FileTools.changePermissions(tmpFile.getAbsolutePath(), true, "755");
        Assert.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath()));
        Assert.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/aFile"));
        Assert.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/sub"));
        Assert.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/sub/aFile"));

        // Test a folder non-recursive
        FileTools.changePermissions(tmpFile.getAbsolutePath(), false, "700");
        Assert.assertEquals("700", FileTools.getPermissions(tmpFile.getAbsolutePath()));
        Assert.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/aFile"));
        Assert.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/sub"));
        Assert.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/sub/aFile"));

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
    public void testWriteFileInputStream() throws Exception {
        File tmpFile = File.createTempFile("junit", null);
        Tuple2<PipedInputStream, PipedOutputStream> pipes = StreamsTools.createPipe();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            FileTools.writeFile(pipes.getA(), tmpFile);
            countDownLatch.countDown();
        }).start();

        PipedOutputStream outputStream = pipes.getB();
        outputStream.write("Test".getBytes(CharsetTools.UTF_8));
        CloseableTools.close(outputStream);

        countDownLatch.await();
        Assert.assertEquals("Test", FileTools.getFileAsString(tmpFile));
    }

    @Test
    public void testWriteFileWithContentCheck() throws Exception {
        // Write content
        File tmpFile = File.createTempFile("junit", null);
        Assert.assertTrue(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), "aaa"));
        Assert.assertFalse(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), "aaa"));
        // Change content
        Assert.assertTrue(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), "bbb"));
        Assert.assertFalse(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), "bbb"));

        // With a List as content
        Assert.assertTrue(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), Arrays.asList("aaa")));
        Assert.assertFalse(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), Arrays.asList("aaa")));

        Assert.assertTrue(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), Arrays.asList("aaa", "bbb")));
        Assert.assertFalse(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), Arrays.asList("aaa", "bbb")));

    }

    @Test
    public void testWriteRead_UTF8() throws Exception {
        File tmpFile = File.createTempFile("junit", null);
        String text = "L'école de la vie";

        Assert.assertTrue(FileTools.writeFile(text, tmpFile));
        String actual = FileTools.getFileAsString(tmpFile);

        Assert.assertEquals(text, actual);

    }

}
