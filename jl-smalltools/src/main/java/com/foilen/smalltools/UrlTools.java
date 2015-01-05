/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some common methods to play with URLs.
 */
public final class UrlTools {

    /**
     * Save the url if the file does not exists and is of a different size.
     * 
     * <pre>
     * Limitations:
     * - If the server does not tell the size, it will always redownload.
     * - It will not resume a download, always restart it.
     * </pre>
     * 
     * @param url
     *            the url to read
     * @param outFile
     *            the destination file
     */
    public static void saveToFile(String url, File outFile) {
        try {
            saveToFile(new URL(url), outFile);
        } catch (MalformedURLException e) {
            throw new SmallToolsException("Invalid URL", e);
        }
    }

    /**
     * Save the url if the file does not exists and is of a different size.
     * 
     * <pre>
     * Limitations:
     * - If the server does not tell the size, it will always redownload.
     * - It will not resume a download, always restart it.
     * </pre>
     * 
     * @param url
     *            the url to read
     * @param outFile
     *            the destination file
     */
    public static void saveToFile(URL url, File outFile) {
        try {
            URLConnection connection = url.openConnection();

            if (!outFile.exists() || outFile.length() != connection.getContentLengthLong()) {
                FileTools.writeFile(connection.getInputStream(), outFile);
            }
        } catch (Exception e) {
            throw new SmallToolsException("Problem downloading the file", e);
        }

    }

}
