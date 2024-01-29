/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import com.foilen.smalltools.compress.UnzipFiles;
import com.foilen.smalltools.compress.ZipFiles;
import com.foilen.smalltools.tools.FileTools;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Test {@link ZipFiles} and {@link UnzipFiles}.
 */
public class ZipAndUnzipFilesTest {

    private void assertFile(File baseDirectory, String filePath, String expectedContent) {
        File file = new File(baseDirectory.getAbsolutePath() + File.separatorChar + filePath);
        Assert.assertTrue(file.exists());
        Assert.assertEquals(expectedContent, FileTools.getFileAsString(file));
    }

    @Test
    public void testZip() throws Exception {

        // Create a temporary file
        File zipFile = File.createTempFile("junit", ".zip");

        // Create the ZIP
        OutputStream outputStream = new FileOutputStream(zipFile);
        ZipFiles zipFiles = new ZipFiles(outputStream);

        // Add files
        zipFiles.addTextFile("firstFile.txt", "Hello World 1");
        zipFiles.addTextFile("test/file inside a dir.txt", "see me inside");
        zipFiles.addTextFile("test/secondInDir.txt", "see me inside 2");

        // Close the ZIP
        zipFiles.close();

        // Unzip as is
        File outputDirectory = Files.createTempDirectory("junit").toFile();
        UnzipFiles unzipFiles = new UnzipFiles(zipFile);
        unzipFiles.extractTo(outputDirectory);

        // Assert
        assertFile(outputDirectory, "firstFile.txt", "Hello World 1");
        assertFile(outputDirectory, "test/file inside a dir.txt", "see me inside");
        assertFile(outputDirectory, "test/secondInDir.txt", "see me inside 2");

        // Unzip ignoring root folder
        outputDirectory = Files.createTempDirectory("junit").toFile();
        unzipFiles = new UnzipFiles(zipFile);
        unzipFiles.setIgnoreFirstSubpath(true);
        unzipFiles.extractTo(outputDirectory);

        // Assert
        assertFile(outputDirectory, "firstFile.txt", "Hello World 1");
        assertFile(outputDirectory, "file inside a dir.txt", "see me inside");
        assertFile(outputDirectory, "secondInDir.txt", "see me inside 2");
    }

}
