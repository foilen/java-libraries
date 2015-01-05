/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools;

import java.io.File;
import java.io.FileOutputStream;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some common methods to manage resources files.
 */
public final class ResourceTools {

    /**
     * Copy a resource to a file.
     * 
     * @param resource
     *            the resource to open
     * @param context
     *            the context class to use relative path
     * @param destinationFile
     *            the file to copy to
     */
    public static void copyToFile(String resource, Class<?> context, File destinationFile) {
        try {
            FileOutputStream fos = new FileOutputStream(destinationFile);
            StreamsTools.flowStream(context.getResourceAsStream(resource), fos);
            fos.close();
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Copy a resource to a file.
     * 
     * @param resource
     *            the absolute resource to open
     * @param destinationFile
     *            the file to copy to
     */
    public static void copyToFile(String resource, File destinationFile) {
        copyToFile(resource, ResourceTools.class, destinationFile);
    }

    /**
     * Load a resource as a String.
     * 
     * @param resource
     *            the resource to open as an absolute path
     * @return the string
     */
    public static String getResourceAsString(String resource) {
        return getResourceAsString(resource, ResourceTools.class);
    }

    /**
     * Load a resource as a String.
     * 
     * @param resource
     *            the resource to open
     * @param context
     *            the context class to use relative path
     * @return the string
     */
    public static String getResourceAsString(String resource, Class<?> context) {
        return StreamsTools.consumeAsString(context.getResourceAsStream(resource));
    }

    private ResourceTools() {
    }
}
