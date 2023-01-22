/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.foilen.smalltools.exception.SmallToolsException;

public class CompressionTools {

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

    static public void gzipFileToFile(File source, String target) {
        gzipFileToFile(source, new File(target));
    }

    static public void gzipFileToFile(String source, File target) {
        gzipFileToFile(new File(source), target);
    }

    static public void gzipFileToFile(String source, String target) {
        gzipFileToFile(new File(source), new File(target));
    }

    private CompressionTools() {
    }

}
