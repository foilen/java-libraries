/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.iterable.FileLinesIterable;

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
     * Load a resource as bytes.
     *
     * @param resource
     *            the resource to open as an absolute path
     * @return the bytes
     */
    public static byte[] getResourceAsBytes(String resource) {
        return getResourceAsBytes(resource, ResourceTools.class);
    }

    /**
     * Load a resource as a bytes.
     *
     * @param resource
     *            the resource to open
     * @param context
     *            the context class to use relative path
     * @return the bytes
     */
    public static byte[] getResourceAsBytes(String resource, Class<?> context) {
        return StreamsTools.consumeAsBytes(context.getResourceAsStream(resource));
    }

    /**
     * Load a resource as a stream.
     *
     * @param resource
     *            the resource to open as an absolute path
     * @return the stream
     */
    public static InputStream getResourceAsStream(String resource) {
        return getResourceAsStream(resource, ResourceTools.class);
    }

    /**
     * Load a resource as a stream.
     *
     * @param resource
     *            the resource to open
     * @param context
     *            the context class to use relative path
     * @return the stream
     */
    public static InputStream getResourceAsStream(String resource, Class<?> context) {
        return context.getResourceAsStream(resource);
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

    /**
     * Get the full path of the containing folder of the resource on the disk.
     *
     * @param classType
     *            the class to get the full path to
     * @return the absolute path to the parent folder
     */
    public static String getResourceDir(Class<?> classType) {
        String className = classType.getSimpleName();
        return getResourceDir(className + ".class", classType);
    }

    /**
     * Get the full path of the containing folder of the resource on the disk.
     *
     * @param resource
     *            the resource name
     * @param context
     *            the context class to use relative path
     * @return the absolute path to the parent folder
     */
    public static String getResourceDir(String resource, Class<?> context) {
        URL url = context.getResource(resource);
        String resourcePath = url.toExternalForm();

        // Remove "file:/"
        resourcePath = resourcePath.substring(6);

        // Remote trailing file name
        int pos = resourcePath.lastIndexOf('/');
        if (pos != -1) {
            resourcePath = resourcePath.substring(0, pos + 1);
        }

        return resourcePath;
    }

    /**
     * Opens a resource and iterates over all the lines.
     *
     * @param resource
     *            the resource to open as an absolute path
     * @return the lines iterable
     */
    public static FileLinesIterable readResourceLinesIteration(String resource) {
        return readResourceLinesIteration(resource, ResourceTools.class);
    }

    /**
     * Opens a resource and iterates over all the lines.
     *
     * @param resource
     *            the resource to open
     * @param context
     *            the context class to use relative path
     * @return the lines iterable
     */
    public static FileLinesIterable readResourceLinesIteration(String resource, Class<?> context) {
        FileLinesIterable fileLinesIterable = new FileLinesIterable();
        fileLinesIterable.openStream(getResourceAsStream(resource, context));
        return fileLinesIterable;
    }

    private ResourceTools() {
    }
}
