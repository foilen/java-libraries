/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.util.Stack;

import com.google.common.base.Joiner;

/**
 * Some common methods to manage directories.
 * 
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:18.0'
 * </pre>
 */
public final class DirectoryTools {

    /**
     * Remove the . and .. from a path
     * 
     * @param path
     *            the path
     * @return the path cleaned up
     */
    public static String cleanupDots(String path) {
        Stack<String> stack = new Stack<String>();

        for (String part : path.split("/")) {
            if (".".equals(part)) {
                // Skip
                continue;
            }

            if ("..".equals(part)) {
                // Remove
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            } else {
                // Add
                stack.push(part);
            }
        }

        // Check if root
        Joiner joiner = Joiner.on("/");
        String result = joiner.join(stack);
        if (path.startsWith("/") && !result.startsWith("/")) {
            result = "/" + result;
        }

        return result;
    }

    /**
     * Create the directory and all the parent ones if needed.
     * 
     * @param directory
     *            the directory
     * @return true if well created or already exists
     */
    public static boolean createPath(File directory) {
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return true;
    }

    /**
     * Create the directory and all the parent ones if needed.
     * 
     * @param directoryPath
     *            the full path
     * @return true if well created or already exists
     */
    public static boolean createPath(String directoryPath) {
        return createPath(new File(directoryPath));
    }

    /**
     * Create the directory and all the parent ones if needed to get to that file.
     * 
     * @param filePath
     *            the full path
     * @return true if well created or already exists
     */
    public static boolean createPathToFile(String filePath) {
        int unixPos = filePath.lastIndexOf("/");
        int windowPos = filePath.lastIndexOf("\\");
        int endOfDirectoryPos = Math.max(unixPos, windowPos);

        if (endOfDirectoryPos == -1) {
            // Current directory
            return true;
        }

        // Create the path
        String directoryPath = filePath.substring(0, endOfDirectoryPos);
        return createPath(new File(directoryPath));
    }

    /**
     * Make sure the path ends with a trailing slash.
     * 
     * @param path
     *            the path to check
     * @return the path with a trailing slash
     */
    public static String pathTrailingSlash(String path) {
        if (!path.isEmpty() && path.charAt(path.length() - 1) != '/') {
            path += '/';
        }

        return path;
    }

    private DirectoryTools() {
    }

}
