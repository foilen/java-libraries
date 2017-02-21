/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.base.Joiner;

/**
 * Some common methods to manage directories.
 *
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:18.0'
 * compile 'org.slf4j:slf4j-api:1.7.21'
 * </pre>
 */
public final class DirectoryTools {

    private final static Logger logger = LoggerFactory.getLogger(DirectoryTools.class);

    /**
     * Remove the . and .. from a path
     *
     * @param path
     *            the path
     * @return the path cleaned up
     */
    public static String cleanupDots(String path) {
        Stack<String> stack = new Stack<>();

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

        logger.debug("createPath {} ", directoryPath);

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
     * Delete the folder and everything inside it. Will not follow symbolic links, but will delete them.
     *
     * WARNING: If you have hard links, it will follow them. (but not symbolic links)
     *
     * @param folder
     *            the folder
     */
    public static void deleteFolder(File folder) {
        logger.info("Delete folder {}", folder.getAbsolutePath());
        if (!folder.exists()) {
            return;
        }

        if (folder.isDirectory()) {
            String rootDir = folder.getAbsolutePath() + File.separator;
            for (File toDelete : folder.listFiles()) {
                deleteSub(rootDir, toDelete);
            }
        }

        if (!folder.delete()) {
            throw new SmallToolsException("Could not delete " + folder.getAbsolutePath());
        }
    }

    /**
     * Delete the folder and everything inside it. Will not follow symbolic links, but will delete them.
     *
     * WARNING: If you have hard links, it will follow them. (but not symbolic links)
     *
     * @param folderPath
     *            the folder
     */
    public static void deleteFolder(String folderPath) {
        deleteFolder(new File(folderPath));
    }

    private static void deleteSub(String rootDir, File folder) {

        String toDelete = folder.getAbsolutePath();
        if (!toDelete.startsWith(rootDir)) {
            throw new SmallToolsException("Trying to delete recursively the folder [" + rootDir + "] we got to delete [" + toDelete + "] which is not a direct child");
        }

        if (!Files.isSymbolicLink(folder.toPath()) && folder.isDirectory()) {
            for (File toDeleteSub : folder.listFiles()) {
                deleteSub(rootDir, toDeleteSub);
            }
        }

        if (!folder.delete()) {
            throw new SmallToolsException("Could not delete " + toDelete);
        }
    }

    /**
     * @deprecated use {@link #listFilesAndFoldersRecursively(File, boolean)}
     */
    @Deprecated
    public static List<String> list(File directory, boolean absolute) {
        return listFilesAndFoldersRecursively(directory, absolute);
    }

    /**
     * @deprecated use {@link #listFilesAndFoldersRecursively(String, boolean)}
     */
    @Deprecated
    public static List<String> list(String path, boolean absolute) {
        return listFilesAndFoldersRecursively(path, absolute);
    }

    /**
     * List files and directories recursively. It can list the absolute or relative paths.
     *
     * Directories will end with a trailing slash.
     *
     *
     * Ex:
     * <ul>
     * <li>foo/bar/aFile</li>
     * <li>foo/bar/aDirectory/</li>
     * <ul>
     *
     * WARNING: It will follow symbolic links.
     *
     * @param directory
     *            the directory
     * @param absolute
     *            true to get the absolute paths
     * @return the names of the files (sorted)
     */
    public static List<String> listFilesAndFoldersRecursively(File directory, boolean absolute) {
        // Check if directory
        if (!directory.isDirectory()) {
            throw new SmallToolsException(directory.getAbsolutePath() + " is not a directory");
        }

        // Scan the directory
        int relativeStartPos = directory.getAbsolutePath().length() + 1;
        List<String> results = listFilesAndFoldersRecursively(directory, absolute, relativeStartPos);

        // Sort
        Collections.sort(results);

        return results;
    }

    private static List<String> listFilesAndFoldersRecursively(File directory, boolean absolute, int relativeStartPos) {
        List<String> results = new ArrayList<>();

        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                if (absolute) {
                    results.add(file.getAbsolutePath());
                } else {
                    results.add(file.getAbsolutePath().substring(relativeStartPos));
                }
            }

            if (file.isDirectory()) {
                if (absolute) {
                    results.add(file.getAbsolutePath() + "/");
                } else {
                    results.add(file.getAbsolutePath().substring(relativeStartPos) + "/");
                }
                results.addAll(listFilesAndFoldersRecursively(file, absolute, relativeStartPos));
            }
        }

        return results;
    }

    /**
     * List files and directories recursively. It can list the absolute or relative paths.
     *
     * Directories will end with a trailing slash.
     *
     * Ex:
     * <ul>
     * <li>foo/bar/aFile</li>
     * <li>foo/bar/aDirectory/</li>
     * <ul>
     *
     * WARNING: It will follow symbolic links.
     *
     * @param path
     *            the full path to the directory
     * @param absolute
     *            true to get the absolute paths
     * @return the names of the files (sorted)
     */
    public static List<String> listFilesAndFoldersRecursively(String path, boolean absolute) {
        return listFilesAndFoldersRecursively(new File(path), absolute);
    }

    /**
     * List the names of the files that the content starts with the specified text.
     *
     * @param path
     *            the full path to the directory
     * @param startText
     *            the text that the files must start with
     * @return the names of the files (sorted)
     */
    public static List<String> listFilesStartingWith(String path, String startText) {
        // Check if directory
        File directory = new File(path);
        if (!directory.isDirectory()) {
            throw new SmallToolsException(path + " is not a directory");
        }

        // Get the bytes
        byte[] startBytes = startText.getBytes(CharsetTools.UTF_8);

        // Scan the directory
        List<String> result = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (!file.isFile()) {
                continue;
            }
            try (InputStream inputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[startBytes.length];
                inputStream.read(buffer);
                if (Arrays.equals(startBytes, buffer)) {
                    result.add(file.getName());
                }
            } catch (Exception e) {
                throw new SmallToolsException("Could not read file " + file.getAbsolutePath(), e);
            }
        }

        // Sort
        Collections.sort(result);

        return result;
    }

    /**
     * List only the files that are in a directory (not anything else like directories).
     *
     * @param directory
     *            the full path to the directory
     * @return the sorted list of file names
     */
    public static List<String> listOnlyFileNames(String directory) {
        return Arrays.asList(new File(directory).listFiles()).stream() //
                .filter(File::isFile) //
                .map(File::getName) //
                .sorted() //
                .collect(Collectors.toList());
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
