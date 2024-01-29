/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.compress;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * Create a zip file with multiple files.
 * <p>
 * Usage:
 *
 * <pre>
 * // Create a temporary file
 * File file = File.createTempFile(&quot;file&quot;, &quot;.zip&quot;);
 * System.out.println(&quot;Temporary file: &quot; + file.getAbsolutePath());
 *
 * // Create the ZIP
 * OutputStream outputStream = new FileOutputStream(file);
 * ZipFiles zipFiles = new ZipFiles(outputStream);
 *
 * // Add files
 * zipFiles.addTextFile(&quot;firstFile.txt&quot;, &quot;Hello World 1&quot;);
 * zipFiles.addTextFile(&quot;test/file inside a dir.txt&quot;, &quot;see me inside&quot;);
 * zipFiles.addTextFile(&quot;test/secondInDir.txt&quot;, &quot;see me inside 2&quot;);
 *
 * // Close the ZIP
 * zipFiles.close();
 * </pre>
 */
public class ZipFiles {

    private ZipOutputStream zos;

    /**
     * Create a ZIP by specifying a file to send it to.
     *
     * @param zipFile the file to write to
     */
    public ZipFiles(File zipFile) {
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException("Could not create the zip", e);
        }
    }

    /**
     * Create a ZIP by specifying a stream to send it to.
     *
     * @param outputStream the stream to write to
     */
    public ZipFiles(OutputStream outputStream) {
        zos = new ZipOutputStream(outputStream);
    }

    /**
     * Add all the files and subdirectories in the zip.
     *
     * @param directoryName the directory name inside the ZIP (E.g: "foo/bar/")
     * @param directory     the local directory to zip
     */
    public void addDirectory(String directoryName, File directory) {

        AssertTools.assertTrue(directory.exists(), "The directory does not exists");
        AssertTools.assertTrue(directory.isDirectory(), "The directory is not a directory");

        // Sanitize
        if (!directoryName.endsWith("/")) {
            directoryName = directoryName + "/";
        }

        // Go through each entries
        for (File entry : directory.listFiles()) {
            String entryName = directoryName + entry.getName();
            if (entry.isFile()) {
                addFile(entryName, entry);
            } else if (entry.isDirectory()) {
                addDirectory(entryName, entry);
            }
        }
    }

    /**
     * Create a file.
     *
     * @param filename the filename inside the ZIP (E.g: "foo/bar.txt")
     * @param file     the local file to zip
     */
    public void addFile(String filename, File file) {
        try {
            addFileFromStream(filename, new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException("The file to zip does not exists", e);
        }
    }

    /**
     * Create a file.
     *
     * @param filename the filename inside the ZIP (E.g: "foo/bar.txt")
     * @param resource the content of the file
     */
    public void addFileFromResource(String filename, String resource) {
        addFileFromStream(filename, ZipFiles.class.getResourceAsStream(resource));
    }

    /**
     * Create a file.
     *
     * @param filename    the filename inside the ZIP (E.g: "foo/bar.txt")
     * @param inputStream the content of the file. Is closed at the end
     */
    public void addFileFromStream(String filename, InputStream inputStream) {
        try {
            // Create entry
            ZipEntry ze = new ZipEntry(filename);
            zos.putNextEntry(ze);

            // Copy content
            StreamsTools.flowStream(inputStream, zos);

            // Close
            zos.closeEntry();
        } catch (IOException e) {
            throw new SmallToolsException("Problem creating the zip file", e);
        }

    }

    /**
     * Create a file.
     *
     * @param filename the filename inside the ZIP (E.g: "foo/bar.txt")
     * @param url      the content of the file
     */
    public void addFileFromUrl(String filename, String url) {
        try {
            addFileFromUrl(filename, new URL(url));
        } catch (MalformedURLException e) {
            throw new SmallToolsException("The url is invalid", e);
        }
    }

    /**
     * Create a file.
     *
     * @param filename the filename inside the ZIP (E.g: "foo/bar.txt")
     * @param url      the content of the file
     */
    public void addFileFromUrl(String filename, URL url) {
        try {
            addFileFromStream(filename, url.openStream());
        } catch (IOException e) {
            throw new SmallToolsException("Problem reading the URL", e);
        }
    }

    /**
     * Create a text file.
     *
     * @param filename the filename inside the ZIP (E.g: "foo/bar.txt")
     * @param content  the content of the file text
     */
    public void addTextFile(String filename, String content) {
        addFileFromStream(filename, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Call this method when all files are added to the ZIP. It will close the provided outputStream in the constructor.
     */
    public void close() {
        try {
            zos.close();
        } catch (IOException e) {
            throw new SmallToolsException("Problem closing the zip file", e);
        }
    }

    /**
     * Change the compression level for the following files.
     *
     * @param level from 0 for none - 9 for best
     */
    public void setCompressionLevel(int level) {
        zos.setLevel(level);
    }

}
