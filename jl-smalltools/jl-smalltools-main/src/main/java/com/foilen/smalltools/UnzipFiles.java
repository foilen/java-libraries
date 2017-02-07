/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.DirectoryTools;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * Take a zip file and extract the content.
 *
 * @deprecated use {@link com.foilen.smalltools.compress.UnzipFiles}
 */
@Deprecated
public class UnzipFiles {

    private ZipInputStream zis;
    private boolean ignoreFirstSubpath = false;

    /**
     * Open a zipped file.
     *
     * @param zipFile
     *            the zipped file
     */
    public UnzipFiles(File zipFile) {
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException("The zip file does not exists", e);
        }
    }

    /**
     * Open a stream.
     *
     * @param inputStream
     *            the zipped stream
     */
    public UnzipFiles(InputStream inputStream) {
        zis = new ZipInputStream(inputStream);
    }

    /**
     * Remove the first folder if {@link #isIgnoreFirstSubpath()}.
     *
     * @param name
     *            the path and name of the zip entry
     * @return the name cleaned
     */
    private String cleanZipName(String name) {
        if (ignoreFirstSubpath) {
            int firstPathPos = name.indexOf("/");
            if (firstPathPos != -1) {
                name = name.substring(firstPathPos + 1);
            }
        }
        return name;
    }

    /**
     * Extract all the files in the specified existing directory.
     *
     * @param destinationDirectory
     *            the directory
     */
    public void extractTo(File destinationDirectory) {

        AssertTools.assertTrue(destinationDirectory.isDirectory(), "The destination is not a directory");

        try {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                // Create the directories
                String fullPath = destinationDirectory.getAbsolutePath() + File.separatorChar + cleanZipName(zipEntry.getName());
                if (!DirectoryTools.createPathToFile(fullPath)) {
                    throw new SmallToolsException("Could not create the directories to " + fullPath);
                }

                if (zipEntry.isDirectory()) {
                    // Create the directory
                    DirectoryTools.createPath(fullPath);
                } else {
                    // Extract the file
                    FileOutputStream fout = new FileOutputStream(fullPath);
                    StreamsTools.flowStream(zis, fout, false);
                    zis.closeEntry();
                    fout.close();
                }

                zipEntry = zis.getNextEntry();
            }
        } catch (Exception e) {
            throw new SmallToolsException("Problem reading the zip", e);
        } finally {
            try {
                zis.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Extract all the files in the specified existing directory.
     *
     * @param destinationDirectory
     *            the directory
     */
    public void extractTo(String destinationDirectory) {
        extractTo(new File(destinationDirectory));
    }

    public boolean isIgnoreFirstSubpath() {
        return ignoreFirstSubpath;
    }

    /**
     * Set to true to skip the first folder in the zip. Good when a zip has a single root folder with the name and version of an application.
     *
     * @param ignoreFirstSubpath
     *            skip the first folder in the zip
     */
    public void setIgnoreFirstSubpath(boolean ignoreFirstSubpath) {
        this.ignoreFirstSubpath = ignoreFirstSubpath;
    }

}
