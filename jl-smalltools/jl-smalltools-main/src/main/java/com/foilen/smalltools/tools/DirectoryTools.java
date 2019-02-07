/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

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
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
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
 * compile 'com.google.guava:guava:23.0'
 * compile 'org.slf4j:slf4j-api:1.7.25'
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
     * Create the directory and all the parent ones if needed. They will all have the owner and group of the first existing parent.
     *
     * @param directory
     *            the directory
     * @return true if well created or already exists
     */
    public static boolean createPathAndCopyOwnerAndGroupFromParent(File directory) {
        if (!directory.exists()) {
            File parentDirectory = directory.getParentFile();
            // Create parent if missing
            if (!parentDirectory.exists()) {
                if (!createPathAndCopyOwnerAndGroupFromParent(parentDirectory)) {
                    return false;
                }
            }
            // Create directory
            if (!directory.mkdir()) {
                logger.error("Could not create directory {}", directory.getAbsolutePath());
                return false;
            }

            // Copy parent owner/group
            FileTools.copyOwnerAndGroupFromParentDir(directory, false);
        }
        return true;
    }

    /**
     * Create the directory and all the parent ones if needed. They will all have the owner and group of the first existing parent.
     *
     * @param directoryPath
     *            the full path
     * @return true if well created or already exists
     */
    public static boolean createPathAndCopyOwnerAndGroupFromParent(String directoryPath) {
        return createPathAndCopyOwnerAndGroupFromParent(new File(directoryPath));
    }

    /**
     * Create the directory and all the parent ones if needed. They will all have the owner and group of the first existing parent.
     *
     * @param directoryPathParts
     *            the path to the directory (e.g new String[] { "/var/vmail/", domain, "/", from, "/Maildir" })
     * @return true if well created or already exists
     */
    public static boolean createPathAndCopyOwnerAndGroupFromParent(String[] directoryPathParts) {
        return createPathAndCopyOwnerAndGroupFromParent(new File(FileTools.concatPath(directoryPathParts)));
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
     * Delete all the sub-folders that are empty.
     *
     * @param rootFolder
     *            the folder
     * @return the amount of folders removed
     */
    public static int deleteEmptySubFolders(File rootFolder) {
        AtomicInteger count = new AtomicInteger();
        visitFilesAndFoldersRecursively(rootFolder, item -> {
            if (item.isDirectory()) {
                logger.debug("Checking if directory {} is empty", item.getPath());
                if (safeListFiles(item).length == 0) {
                    logger.info("Deleting directory {} because it is empty", item.getPath());
                    count.incrementAndGet();
                    if (!item.delete()) {
                        logger.error("Could not delete folder {}", item.getPath());
                    }
                }
            }
        });
        return count.get();
    }

    /**
     * Delete all the sub-folders that are empty.
     *
     * @param rootFolder
     *            the folder
     * @return the amount of folders removed
     */
    public static int deleteEmptySubFolders(String rootFolder) {
        return deleteEmptySubFolders(new File(rootFolder));
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
            for (File toDelete : safeListFiles(folder)) {
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

    /**
     * Delete all the files that are older (modified time) than the specified date in the folder and sub-folders.
     *
     * @param rootFolder
     *            the folder
     * @param beforeDate
     *            the date of the modified time
     * @return the amount of files removed
     */
    public static int deleteOlderFilesInDirectory(File rootFolder, Date beforeDate) {
        AtomicInteger count = new AtomicInteger();
        long expiredBefore = beforeDate.getTime();
        visitFilesAndFoldersRecursively(rootFolder, item -> {
            if (item.isFile()) {
                logger.debug("Checking if file {} is too old", item.getPath());
                if (item.lastModified() < expiredBefore) {
                    logger.info("Deleting file {} . Last modified time: {}", item.getPath(), DateTools.formatFull(new Date(item.lastModified())));
                    count.incrementAndGet();
                    if (!item.delete()) {
                        logger.error("Could not delete file {}", item.getPath());
                    }
                }
            }
        });
        return count.get();
    }

    /**
     * Delete all the files that are older (modified time) than the specified date in the folder and sub-folders.
     *
     * @param rootFolder
     *            the folder
     * @param beforeDate
     *            the date of the modified time
     * @return the amount of files removed
     */
    public static int deleteOlderFilesInDirectory(String rootFolder, Date beforeDate) {
        return deleteOlderFilesInDirectory(new File(rootFolder), beforeDate);
    }

    private static void deleteSub(String rootDir, File folder) {

        String toDelete = folder.getAbsolutePath();
        if (!toDelete.startsWith(rootDir)) {
            throw new SmallToolsException("Trying to delete recursively the folder [" + rootDir + "] we got to delete [" + toDelete + "] which is not a direct child");
        }

        if (!Files.isSymbolicLink(folder.toPath()) && folder.isDirectory()) {
            for (File toDeleteSub : safeListFiles(folder)) {
                deleteSub(rootDir, toDeleteSub);
            }
        }

        if (!folder.delete()) {
            throw new SmallToolsException("Could not delete " + toDelete);
        }
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
     * </ul>
     *
     * WARNING: It will follow symbolic links.
     *
     * @param directory
     *            the directory
     * @param absolute
     *            true to get the absolute paths
     * @return the names of the files (sorted)
     * @deprecated use {@link #listFilesAndFoldersRecursively(File, boolean)}
     */
    @Deprecated
    public static List<String> list(File directory, boolean absolute) {
        return listFilesAndFoldersRecursively(directory, absolute);
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
     * </ul>
     *
     * WARNING: It will follow symbolic links.
     *
     * @param path
     *            the full path to the directory
     * @param absolute
     *            true to get the absolute paths
     * @return the names of the files (sorted)
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
     * </ul>
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
     * </ul>
     *
     * WARNING: It will follow symbolic links.
     *
     * @param directory
     *            the directory
     * @param absolute
     *            true to get the absolute paths
     * @return the names of the files (sorted)
     */
    private static List<String> listFilesAndFoldersRecursively(File directory, boolean absolute, int relativeStartPos) {
        List<String> results = new ArrayList<>();

        for (File file : safeListFiles(directory)) {
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
     * </ul>
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
        for (File file : safeListFiles(directory)) {
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
        return Arrays.asList(safeListFiles(new File(directory))).stream() //
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

    /**
     * List the content of the directory. Does not return null, but an empty list.
     *
     * @param directory
     *            the directory to list
     * @return the list of files or empty list if null
     */
    public static File[] safeListFiles(File directory) {
        File[] files = directory.listFiles();
        return files == null ? new File[] {} : files;
    }

    /**
     * Visit all the files and folders in sub-directories. When visiting a folder, will do it after visiting everything inside it.
     *
     * @param directory
     *            the directory
     * @param fileAction
     *            the action to execute on each file
     */
    public static void visitFilesAndFoldersRecursively(File directory, Consumer<File> fileAction) {

        // Check if directory
        if (!directory.isDirectory()) {
            throw new SmallToolsException(directory.getAbsolutePath() + " is not a directory");
        }

        // Scan the directory
        for (File file : safeListFiles(directory)) {
            if (file.isFile()) {
                fileAction.accept(file);
            }

            if (file.isDirectory()) {
                visitFilesAndFoldersRecursively(file, fileAction);
                fileAction.accept(file);
            }
        }

    }

    /**
     * Visit all the files and folders in sub-directories. Will do it in depth-first order.
     *
     * @param directory
     *            the directory
     * @param fileAction
     *            the action to execute on each file
     */
    public static void visitFilesAndFoldersRecursively(String directory, Consumer<File> fileAction) {
        visitFilesAndFoldersRecursively(new File(directory), fileAction);
    }

    private DirectoryTools() {
    }

}
