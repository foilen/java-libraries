/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Some common methods to manage directories.
 * 
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:18.0'
 * compile 'org.slf4j:slf4j-api:1.7.12'
 * </pre>
 */
public final class DirectoryTools {

    private final static Logger log = LoggerFactory.getLogger(DirectoryTools.class);

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
     * Create the directory and all the parent ones if needed. Only the final folder will have the owner, group and permissions.
     * 
     * @param directoryPath
     *            the path to the directory
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if well created or already exists
     */
    public static boolean createPath(String directoryPath, String owner, String group, String permissions) {

        log.debug("createPath {} ", directoryPath);

        // Create
        if (!createPath(directoryPath)) {
            return false;
        }
        // Change owner and permission
        FileTools.changeOwnerAndGroup(directoryPath, false, owner, group);
        FileTools.changePermissions(directoryPath, false, permissions);

        return true;
    }

    /**
     * Create the directory and all the parent ones if needed.
     * 
     * @param directoryPathParts
     *            the path to the directory (e.g new String[] { "/var/vmail/", domain, "/", from, "/Maildir" })
     * @return true if well created or already exists
     */
    public static boolean createPath(String[] directoryPathParts) {
        return createPath(new File(FileTools.concatPath(directoryPathParts)));
    }

    /**
     * Create the directory and all the parent ones if needed. Only the final folder will have the owner, group and permissions.
     * 
     * @param directoryPathParts
     *            the path to the directory (e.g new String[] { "/var/vmail/", domain, "/", from, "/Maildir" })
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if well created or already exists
     */
    public static boolean createPath(String[] directoryPathParts, String owner, String group, String permissions) {
        return createPath(FileTools.concatPath(directoryPathParts), owner, group, permissions);
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
