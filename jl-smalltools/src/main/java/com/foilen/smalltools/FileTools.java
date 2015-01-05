/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.UserPrincipal;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some common methods to manage files.
 */
public final class FileTools {

    /**
     * Change the parent directory owner and group and copy them to the specified file or directory.
     * 
     * @param fileOrDirectory
     *            the file or directory to modify the owners
     * @param recursive
     *            true to copy to all files and directories
     */
    public static void changeOwnerAndGroup(File fileOrDirectory, boolean recursive, UserPrincipal owner, GroupPrincipal group) {

        try {
            // Change the owners of the current directory or file
            Path fileOrDirectoryPath = fileOrDirectory.toPath();
            PosixFileAttributeView view = Files.getFileAttributeView(fileOrDirectoryPath, PosixFileAttributeView.class);
            view.setOwner(owner);
            view.setGroup(group);

            // If recursive and is a directory, update all entries in that directory
            if (recursive && fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    changeOwnerAndGroup(child, recursive, owner, group);
                }
            }
        } catch (IOException e) {
            throw new SmallToolsException("Problem setting owner or group", e);
        }
    }

    /**
     * Take the parent directory owner and group and copy them to the specified file or directory.
     * 
     * @param fileOrDirectory
     *            the file or directory to modify the owners
     * @param recursive
     *            true to copy to all files and directories
     */
    public static void copyOwnerAndGroupFromParentDir(File fileOrDirectory, boolean recursive) {
        Path fileOrDirectoryPath = fileOrDirectory.toPath();
        Path parent = fileOrDirectoryPath.getParent();
        if (parent == null) {
            throw new SmallToolsException("There is no parent to " + fileOrDirectory.getAbsolutePath());
        }
        try {
            // Get the parent information
            PosixFileAttributeView view = Files.getFileAttributeView(parent, PosixFileAttributeView.class);
            PosixFileAttributes attributes = view.readAttributes();
            UserPrincipal owner = attributes.owner();
            GroupPrincipal group = attributes.group();

            // Set the information
            changeOwnerAndGroup(fileOrDirectory, recursive, owner, group);

        } catch (IOException e) {
            throw new SmallToolsException("Problem setting owner or group", e);
        }
    }

    /**
     * Take the parent directory owner and group and copy them to the specified file or directory.
     * 
     * @param fileOrDirectory
     *            the file or directory to modify the owners
     * @param recursive
     *            true to copy to all files and directories
     */
    public static void copyOwnerAndGroupFromParentDir(String fileOrDirectory, boolean recursive) {
        copyOwnerAndGroupFromParentDir(new File(fileOrDirectory), recursive);
    }

    /**
     * Gives an absolute path. If the path is relative, the absolute will be composed of the working directory and the file.
     * 
     * @param workingDirectory
     *            the working directory if filePath is relative. It can end with the path separator or not.
     * @param filePath
     *            the path of the file (absolute or relative)
     * @return the absolute path
     */
    public static String getAbsolutePath(String workingDirectory, String filePath) {
        String result = "";

        // Analyze
        boolean backSlash = workingDirectory.contains("\\");
        if (backSlash) {
            workingDirectory = workingDirectory.replace("\\", "/");
        }
        if (!workingDirectory.endsWith("/")) {
            workingDirectory = workingDirectory + "/";
        }
        boolean windowsPath = isWindowsStartPath(workingDirectory);
        filePath = filePath.replace("\\", "/");

        // Absolute
        if (filePath.startsWith("/") || (windowsPath && isWindowsStartPath(filePath))) {
            result = filePath;
        } else {
            // Relative

            // Store root
            String root = "/";
            if (windowsPath) {
                root = workingDirectory.substring(0, 3);
            }

            // Remove the root and add the destination
            String current = workingDirectory.substring(root.length()) + filePath;

            // Cleanup the ..
            current = DirectoryTools.cleanupDots(current);

            // Replace the root
            result = root + current;
        }

        // Fix the slashes
        if (backSlash) {
            result = result.replace("/", "\\");
        }

        return result;
    }

    /**
     * File as an array of bytes.
     * 
     * @param file
     *            the file to open
     * @return the bytes
     */
    public static byte[] getFileAsBytes(File file) {
        try {
            return StreamsTools.consumeAsBytes(new FileInputStream(file));
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Load a file as a String.
     * 
     * @param file
     *            the file to open
     * @return the string
     */
    public static String getFileAsString(File file) {
        try {
            return StreamsTools.consumeAsString(new FileInputStream(file));
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Load a file as a String.
     * 
     * @param fileName
     *            the file path to open
     * @return the string
     */
    public static String getFileAsString(String fileName) {
        try {
            return StreamsTools.consumeAsString(new FileInputStream(fileName));
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Retrieve the owner of the file or directory.
     * 
     * @param file
     *            the file or directory to get the owner
     * @return the owner
     */
    public static String getOwner(File file) {
        try {
            Path path = file.toPath();
            PosixFileAttributeView view = Files.getFileAttributeView(path, PosixFileAttributeView.class);
            return view.getOwner().getName();
        } catch (IOException e) {
            throw new SmallToolsException("Problem getting owner", e);
        }
    }

    /**
     * Tells if the path is an absolute Windows one.
     * 
     * @param path
     *            the path
     * @return true if it is an absolute Windows one
     */
    public static boolean isWindowsStartPath(String path) {
        // Starts with letter, : and \
        return path.matches("^[a-zA-Z]\\:[\\\\/].*$");
    }

    /**
     * Opens a file and iterates over all the lines.
     * 
     * @param file
     *            the file
     * @return an iterable
     * @throws FileNotFoundException
     *             FileNotFoundException
     */
    public static FileLinesIterable readFileLinesIteration(File file) throws FileNotFoundException {
        FileLinesIterable result = new FileLinesIterable();
        result.openFile(file);
        return result;
    }

    /**
     * Opens a file and iterates over all the lines.
     * 
     * @param filePath
     *            the absolute file path
     * @return an iterable
     * @throws FileNotFoundException
     *             FileNotFoundException
     */
    public static FileLinesIterable readFileLinesIteration(String filePath) throws FileNotFoundException {
        return readFileLinesIteration(new File(filePath));
    }

    /**
     * Save the stream to a file.
     * 
     * @param inputStream
     *            the content to write
     * @param outFile
     *            the file to write into
     * @return true if it worked
     */
    public static boolean writeFile(InputStream inputStream, File outFile) {
        try {
            FileOutputStream fos = new FileOutputStream(outFile);
            StreamsTools.flowStream(inputStream, fos);
            fos.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Save some texts to a file.
     * 
     * @param content
     *            the content to write
     * @param file
     *            the file to write into
     * @return true if it worked
     */
    public static boolean writeFile(String content, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
