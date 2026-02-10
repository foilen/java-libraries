package com.foilen.smalltools.tools;

import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.tuple.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class FileToolsTest {

    private void assertFileContent(File tmpExpected, File tmpActual) {
        String expected = FileTools.getFileAsString(tmpExpected);
        String actual = FileTools.getFileAsString(tmpActual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testAppendLineAndReadFileLinesStream() throws Exception {
        File tmpFile = File.createTempFile("junit", null);
        FileTools.appendLine(tmpFile, "hello world");
        FileTools.appendLine(tmpFile, "aligator");
        FileTools.appendLine(tmpFile.getAbsolutePath(), "yep");

        List<String> lines = FileTools.readFileLinesStream(tmpFile).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList("hello world", "aligator", "yep"), lines);

        lines = FileTools.readFileLinesStream(tmpFile.getAbsolutePath()).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList("hello world", "aligator", "yep"), lines);
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
        Assertions.assertFalse(FileTools.exists(tmpFile.getAbsolutePath()));

        // Create empty
        FileTools.clearFile(tmpFile);
        Assertions.assertTrue(FileTools.exists(tmpFile.getAbsolutePath()));
        Assertions.assertEquals(0, FileTools.getFileAsBytes(tmpFile).length);

        // Put some things in it and clear it
        FileTools.writeFile("hello", tmpFile);
        Assertions.assertTrue(FileTools.exists(tmpFile.getAbsolutePath()));
        Assertions.assertNotEquals(0, FileTools.getFileAsBytes(tmpFile).length);

        FileTools.clearFile(tmpFile.getAbsolutePath());
        Assertions.assertTrue(FileTools.exists(tmpFile.getAbsolutePath()));
        Assertions.assertEquals(0, FileTools.getFileAsBytes(tmpFile).length);

        // Delete
        FileTools.deleteFile(tmpFile.getAbsolutePath());
        Assertions.assertFalse(FileTools.exists(tmpFile.getAbsolutePath()));
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
        Assertions.assertTrue(stagingFile.exists());
        Assertions.assertFalse(finalFile.exists());

        // Put some data
        outputStream.write("yay".getBytes(StandardCharsets.UTF_8));
        Assertions.assertTrue(stagingFile.exists());
        Assertions.assertFalse(finalFile.exists());

        // Close and check final
        outputStream.close();
        Assertions.assertFalse(stagingFile.exists());
        Assertions.assertTrue(finalFile.exists());

        Assertions.assertEquals("yay", FileTools.getFileAsString(finalFile));
    }

    @Test
    public void testGetAbsolutePathAbsolute() {
        // Unix
        String workingDirectory = "/tmp/";
        String destinationPath = "/home/junit/file";
        String actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals(destinationPath, actual);

        // Windows lower case
        workingDirectory = "c:\\tmp\\";
        destinationPath = "c:\\windows\\notepad.exe";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals(destinationPath, actual);

        // Windows upper case
        workingDirectory = "C:\\tmp\\";
        destinationPath = "C:\\windows\\notepad.exe";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals(destinationPath, actual);
    }

    @Test
    public void testGetAbsolutePathRelative() {
        // Unix forward
        String workingDirectory = "/tmp/";
        String destinationPath = "junit/file";
        String actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("/tmp/junit/file", actual);

        // Unix backward one
        workingDirectory = "/tmp/junit/";
        destinationPath = "../junit2/file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("/tmp/junit2/file", actual);

        // Unix backward two
        workingDirectory = "/tmp/dir1/dir2/";
        destinationPath = "../../junit2/file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("/tmp/junit2/file", actual);

        // Unix backward a lot
        workingDirectory = "/tmp/dir1/";
        destinationPath = "../../../../../junit2/file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("/junit2/file", actual);

        // Windows forward
        workingDirectory = "c:\\tmp\\";
        destinationPath = "junit\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("c:\\tmp\\junit\\file", actual);

        // Windows backward one
        workingDirectory = "c:\\tmp\\junit\\";
        destinationPath = "..\\junit2\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("c:\\tmp\\junit2\\file", actual);

        // Windows backward two
        workingDirectory = "c:\\tmp\\dir1\\dir2\\";
        destinationPath = "..\\..\\junit2\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("c:\\tmp\\junit2\\file", actual);

        // Windows backward a lot
        workingDirectory = "c:\\tmp\\dir1\\";
        destinationPath = "..\\..\\..\\..\\..\\junit2\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("c:\\junit2\\file", actual);

        // Unix forward without ending separator
        workingDirectory = "/tmp";
        destinationPath = "junit/file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("/tmp/junit/file", actual);

        // Windows forward without ending separator
        workingDirectory = "c:\\tmp";
        destinationPath = "junit\\file";
        actual = FileTools.getAbsolutePath(workingDirectory, destinationPath);
        Assertions.assertEquals("c:\\tmp\\junit\\file", actual);
    }

    @Test
    public void testGetExtension() {
        Assertions.assertEquals("js", FileTools.getExtension("all.js"));
        Assertions.assertNull(FileTools.getExtension("all-test"));
        Assertions.assertEquals("", FileTools.getExtension("all."));

        Assertions.assertEquals("js", FileTools.getExtension("/tmp.test/all.js"));
        Assertions.assertNull(FileTools.getExtension("/tmp.test/all-test"));
        Assertions.assertEquals("", FileTools.getExtension("/tmp.test/all."));
    }

    @Test
    public void testIsWindowsStartPath() {
        Assertions.assertTrue(FileTools.isWindowsStartPath("c:\\windows\\notepad.exe"));
        Assertions.assertTrue(FileTools.isWindowsStartPath("C:\\windows\\notepad.exe"));
        Assertions.assertTrue(FileTools.isWindowsStartPath("c:/windows/note:pad.exe"));
        Assertions.assertFalse(FileTools.isWindowsStartPath("/windows/note:pad.exe"));
        Assertions.assertFalse(FileTools.isWindowsStartPath("\\windows\\note:pad.exe"));
        Assertions.assertFalse(FileTools.isWindowsStartPath("c\\windows\\notepad.exe"));
    }

    @Test
    public void testPermissions() throws IOException {

        if (JavaEnvironmentValues.getOperatingSystem().toLowerCase().startsWith("windows")) {
            return;
        }

        File tmpFile = File.createTempFile("junit", null);

        // Test a file
        FileTools.changePermissions(tmpFile.getAbsolutePath(), false, "755");
        Assertions.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath()));

        FileTools.changePermissions(tmpFile.getAbsolutePath(), false, "644");
        Assertions.assertEquals("644", FileTools.getPermissions(tmpFile.getAbsolutePath()));

        // Test a folder recursive
        tmpFile.delete();
        Assertions.assertTrue(DirectoryTools.createPath(tmpFile.getAbsolutePath() + "/sub"));
        FileTools.writeFile("hello", tmpFile.getAbsolutePath() + "/sub/aFile");
        FileTools.writeFile("hello", tmpFile.getAbsolutePath() + "/aFile");
        FileTools.changePermissions(tmpFile.getAbsolutePath(), true, "755");
        Assertions.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath()));
        Assertions.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/aFile"));
        Assertions.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/sub"));
        Assertions.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/sub/aFile"));

        // Test a folder non-recursive
        FileTools.changePermissions(tmpFile.getAbsolutePath(), false, "700");
        Assertions.assertEquals("700", FileTools.getPermissions(tmpFile.getAbsolutePath()));
        Assertions.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/aFile"));
        Assertions.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/sub"));
        Assertions.assertEquals("755", FileTools.getPermissions(tmpFile.getAbsolutePath() + "/sub/aFile"));

    }

    @Test
    public void testReadFileLinesIteration() throws IOException {
        File tmpFile = File.createTempFile("junit", null);
        String content = "This is the first line\nAnd the second one\nA last one";
        Assertions.assertTrue(FileTools.writeFile(content, tmpFile));

        String[] parts = content.split("\n");
        int count = 0;
        for (String nextLine : FileTools.readFileLinesIteration(tmpFile.getAbsolutePath())) {
            Assertions.assertEquals(parts[count++], nextLine);
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
        outputStream.write("Test".getBytes(StandardCharsets.UTF_8));
        CloseableTools.close(outputStream);

        countDownLatch.await();
        Assertions.assertEquals("Test", FileTools.getFileAsString(tmpFile));
    }

    @Test
    public void testWriteFileWithContentCheck() throws Exception {
        // Write content
        File tmpFile = File.createTempFile("junit", null);
        Assertions.assertTrue(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), "aaa"));
        Assertions.assertFalse(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), "aaa"));
        // Change content
        Assertions.assertTrue(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), "bbb"));
        Assertions.assertFalse(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), "bbb"));

        // With a List as content
        Assertions.assertTrue(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), Arrays.asList("aaa")));
        Assertions.assertFalse(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), Arrays.asList("aaa")));

        Assertions.assertTrue(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), Arrays.asList("aaa", "bbb")));
        Assertions.assertFalse(FileTools.writeFileWithContentCheck(tmpFile.getAbsolutePath(), Arrays.asList("aaa", "bbb")));

    }

    @Test
    public void testWriteRead_UTF8() throws Exception {
        File tmpFile = File.createTempFile("junit", null);
        String text = "L'école de la vie";

        Assertions.assertTrue(FileTools.writeFile(text, tmpFile));
        String actual = FileTools.getFileAsString(tmpFile);

        Assertions.assertEquals(text, actual);

    }

}
