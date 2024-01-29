/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;

import java.io.*;
import java.util.zip.GZIPOutputStream;

/**
 * Tools to compress and uncompress files.
 */
public class CompressionTools {

    /**
     * Gzip a file.
     *
     * @param source the source file
     * @param target the target compressed file
     */
    static public void gzipFileToFile(File source, File target) {

        OutputStream out = null;
        try {
            out = new GZIPOutputStream(new FileOutputStream(target));
            InputStream in = new FileInputStream(source);
            StreamsTools.flowStream(in, out);
        } catch (Exception e) {
            throw new SmallToolsException("Problem gzipping the file", e);
        } finally {
            CloseableTools.close(out);
        }
    }

    /**
     * Gzip a file.
     *
     * @param source the source file
     * @param target the target compressed file
     */
    static public void gzipFileToFile(File source, String target) {
        gzipFileToFile(source, new File(target));
    }

    /**
     * Gzip a file.
     *
     * @param source the source file
     * @param target the target compressed file
     */
    static public void gzipFileToFile(String source, File target) {
        gzipFileToFile(new File(source), target);
    }

    /**
     * Gzip a file.
     *
     * @param source the source file
     * @param target the target compressed file
     */
    static public void gzipFileToFile(String source, String target) {
        gzipFileToFile(new File(source), new File(target));
    }

    private CompressionTools() {
    }

}
