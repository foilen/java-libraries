/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.FileLinesIterable;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.hash.HashMd5sum;
import com.foilen.smalltools.streamwrapper.RenamingOnCloseOutputStreamWrapper;
import com.google.common.base.Joiner;

/**
 * Some common methods to manage files.
 * 
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:18.0'
 * compile 'org.slf4j:slf4j-api:1.7.12'
 * </pre>
 */
public final class FileTools {

    private final static Logger log = LoggerFactory.getLogger(FileTools.class);

    private static final Joiner LINES_JOINER = Joiner.on('\n');
    private static final UserPrincipalLookupService USER_PRINCIPAL_LOOKUP_SERVICE = FileSystems.getDefault().getUserPrincipalLookupService();

    /**
     * Check if the file does not contain the line and append it if missing.
     * 
     * @param path
     *            the path to the file
     * @param line
     *            the line to add
     */
    public static void appendLineIfMissing(String path, String line) {
        // Search
        boolean endsWithEmptyLine = false;
        OutputStream out = null;
        File file = new File(path);
        try {
            for (String currLine : readFileLinesIteration(file)) {
                if (currLine.equals(line)) {
                    return;
                }
            }

            // Check last character
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(file);
                fin.skip(file.length() - 1);
                char c = (char) fin.read();
                endsWithEmptyLine = c == '\n' || c == '\r';
            } catch (Exception e) {
            } finally {
                CloseableTools.close(fin);
            }

            // Open for appending
            out = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            endsWithEmptyLine = true;
            try {
                // Open new file
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e1) {
            }
        }

        // Write
        try {
            if (endsWithEmptyLine) {
                out.write((line + "\n").getBytes());
            } else {
                out.write(("\n" + line + "\n").getBytes());
            }
        } catch (Exception e) {
            throw new SmallToolsException("Problem writing to file", e);
        } finally {
            CloseableTools.close(out);
        }
    }

    /**
     * Change the owner and group of the specified file or directory.
     * 
     * @param fileOrDirectory
     *            the file or directory to modify the owners
     * @param recursive
     *            true to change to all files and directories
     * @param owner
     *            the owner
     * @param group
     *            the group
     */
    public static void changeOwnerAndGroup(File fileOrDirectory, boolean recursive, UserPrincipal owner, GroupPrincipal group) {

        // Check if exists
        if (!fileOrDirectory.exists()) {
            throw new SmallToolsException("The file or directory " + fileOrDirectory.getAbsolutePath() + " does not exists");
        }

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
     * Change the owner and group of the specified file or directory.
     * 
     * @param fileOrDirectory
     *            the file or directory to modify the owners
     * @param recursive
     *            true to change to all files and directories
     * @param owner
     *            the owner
     * @param group
     *            the group
     */
    public static void changeOwnerAndGroup(String fileOrDirectory, boolean recursive, String owner, String group) {
        try {
            File file = new File(fileOrDirectory);
            UserPrincipal userPrincipal = USER_PRINCIPAL_LOOKUP_SERVICE.lookupPrincipalByName(owner);
            GroupPrincipal groupPrincipal = USER_PRINCIPAL_LOOKUP_SERVICE.lookupPrincipalByGroupName(group);
            changeOwnerAndGroup(file, recursive, userPrincipal, groupPrincipal);
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Problem setting owner or group", e);
        }

    }

    /**
     * Change the POSIX permissions of the specified file or directory.
     * 
     * @param fileOrDirectory
     *            the file or directory to modify the permissions
     * @param recursive
     *            true to change to all files and directories
     * @param permissions
     *            the list of permissions
     */
    public static void changePermissions(File fileOrDirectory, boolean recursive, Set<PosixFilePermission> permissions) {

        // Check if exists
        if (!fileOrDirectory.exists()) {
            throw new SmallToolsException("The file or directory " + fileOrDirectory.getAbsolutePath() + " does not exists");
        }

        try {

            // Change permissions
            Path path = fileOrDirectory.toPath();
            Files.setPosixFilePermissions(path, permissions);

            // If recursive and is a directory, update all entries in that directory
            if (recursive && fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    changePermissions(child, recursive, permissions);
                }
            }
        } catch (IOException e) {
            throw new SmallToolsException("Problem setting the permissions", e);
        }
    }

    /**
     * Change the POSIX permissions of the specified file or directory.
     * 
     * @param fileOrDirectory
     *            the file or directory to modify the permissions
     * @param recursive
     *            true to change to all files and directories
     * @param permissions
     *            the numeric permissions (e.g "777")
     */
    public static void changePermissions(String fileOrDirectory, boolean recursive, String permissions) {
        File file = new File(fileOrDirectory);
        Set<PosixFilePermission> permissionsSet = new HashSet<>();

        // Get the permissions
        if (permissions.length() != 3) {
            throw new SmallToolsException("The permissions must be like 777. Current: " + permissions);
        }

        String[] parts = new String[3];
        for (int i = 0; i < 3; ++i) {
            parts[i] = String.valueOf(permissions.charAt(i));
        }
        // Owner
        Integer ip = Integer.valueOf(parts[0]);
        if (isPermRead(ip)) {
            permissionsSet.add(PosixFilePermission.OWNER_READ);
        }
        if (isPermWrite(ip)) {
            permissionsSet.add(PosixFilePermission.OWNER_WRITE);
        }
        if (isPermExecute(ip)) {
            permissionsSet.add(PosixFilePermission.OWNER_EXECUTE);
        }

        // Group
        ip = Integer.valueOf(parts[1]);
        if (isPermRead(ip)) {
            permissionsSet.add(PosixFilePermission.GROUP_READ);
        }
        if (isPermWrite(ip)) {
            permissionsSet.add(PosixFilePermission.GROUP_WRITE);
        }
        if (isPermExecute(ip)) {
            permissionsSet.add(PosixFilePermission.GROUP_EXECUTE);
        }

        // Other
        ip = Integer.valueOf(parts[2]);
        if (isPermRead(ip)) {
            permissionsSet.add(PosixFilePermission.OTHERS_READ);
        }
        if (isPermWrite(ip)) {
            permissionsSet.add(PosixFilePermission.OTHERS_WRITE);
        }
        if (isPermExecute(ip)) {
            permissionsSet.add(PosixFilePermission.OTHERS_EXECUTE);
        }

        changePermissions(file, recursive, permissionsSet);
    }

    protected static String concatPath(String... pathParts) {
        StringBuilder sb = new StringBuilder();
        for (String pathPart : pathParts) {
            sb.append(pathPart);
        }

        return sb.toString();
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
     * Create a staging file to write to and when you will close it, it will rename to its final destination.
     * 
     * @param stagingFile
     *            the file to write to temporarily
     * @param finalFile
     *            when closing, rename the staging file to this file
     * @return the outputstream to write to the staging file and that needs to be closed to rename
     */
    public static OutputStream createStagingFile(File stagingFile, File finalFile) {
        try {
            FileOutputStream outputStream = new FileOutputStream(stagingFile);
            return new RenamingOnCloseOutputStreamWrapper(outputStream, stagingFile, finalFile);
        } catch (Exception e) {
            throw new SmallToolsException("Problem creating the staging file", e);
        }
    }

    /**
     * Create a staging file to write to and when you will close it, it will rename to its final destination.
     * 
     * @param stagingFileName
     *            the file to write to temporarily
     * @param finalFileName
     *            when closing, rename the staging file to this file
     * @return the outputstream to write to the staging file and that needs to be closed to rename
     */
    public static OutputStream createStagingFile(String stagingFileName, String finalFileName) {
        return createStagingFile(new File(stagingFileName), new File(finalFileName));
    }

    /**
     * Delete the file.
     * 
     * @param path
     *            the path to the file
     * @return true if the file was removed
     */
    public static boolean deleteFile(String path) {
        return new File(path).delete();
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

    private static boolean isPermExecute(int perm) {
        return perm == 1 || perm == 3 || perm == 5 || perm == 7;
    }

    private static boolean isPermRead(int perm) {
        return perm >= 4;
    }

    private static boolean isPermWrite(int perm) {
        return perm == 2 || perm == 3 || perm == 6 || perm == 7;
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
        byte[] startBytes = startText.getBytes();
        byte[] buffer = new byte[startBytes.length];

        // Scan the directory
        List<String> result = new ArrayList<>();
        for (File file : directory.listFiles()) {
            try (InputStream inputStream = new FileInputStream(file)) {
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
     */
    public static FileLinesIterable readFileLinesIteration(String filePath) {
        try {
            return readFileLinesIteration(new File(filePath));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException("Problem reading the file", e);
        }
    }

    /**
     * Save the stream to a file.
     * 
     * @param inputStream
     *            the content to write
     * @param file
     *            the file to write into
     * @return true if it worked
     */
    public static boolean writeFile(InputStream inputStream, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            StreamsTools.flowStream(inputStream, fos);
            fos.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Save the stream to a file.
     * 
     * @param inputStream
     *            the content to write
     * @param file
     *            the file to write into
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if it worked
     */
    public static boolean writeFile(InputStream inputStream, File file, String owner, String group, String permissions) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            StreamsTools.flowStream(inputStream, fos);
            fos.close();

            // Update owners and permissions
            String path = file.getAbsolutePath();
            changeOwnerAndGroup(path, false, owner, group);
            changePermissions(path, false, permissions);

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

    /**
     * Save some texts to a file.
     * 
     * @param content
     *            the content to write
     * @param file
     *            the file to write into
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if it worked
     */
    public static boolean writeFile(String content, File file, String owner, String group, String permissions) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();

            // Update owners and permissions
            String path = file.getAbsolutePath();
            changeOwnerAndGroup(path, false, owner, group);
            changePermissions(path, false, permissions);

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
     * @param path
     *            the path to the file
     */
    public static void writeFile(String content, String path) {
        writeFile(content, new File(path));
    }

    /**
     * Save some texts to a file.
     * 
     * @param path
     *            the path to the file
     * @param contentLines
     *            all the lines of text
     * @return true if the file was created or the content is different
     */
    public static boolean writeFileWithContentCheck(String path, List<String> contentLines) {
        return writeFileWithContentCheck(path, LINES_JOINER.join(contentLines));
    }

    /**
     * Save some texts to a file.
     * 
     * @param path
     *            the path to the file
     * @param contentLines
     *            all the lines of text
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if the file was created or the content is different
     */
    public static boolean writeFileWithContentCheck(String path, List<String> contentLines, String owner, String group, String permissions) {

        boolean needUpdate = writeFileWithContentCheck(path, contentLines);

        // Update owners and permissions
        changeOwnerAndGroup(path, false, owner, group);
        changePermissions(path, false, permissions);

        return needUpdate;
    }

    /**
     * Save some texts to a file.
     * 
     * @param path
     *            the path to the file
     * @param content
     *            the text content
     * @return true if the file was created or the content is different
     */
    public static boolean writeFileWithContentCheck(String path, String content) {

        log.debug("writeFileWithContentCheck {}", path);

        boolean needUpdate = false;
        String contentMd5 = HashMd5sum.hashString(content);
        File file = new File(path);

        if (file.exists()) {
            // Existing file
            String fileMd5 = HashMd5sum.hashFile(file);
            log.debug("Content md5 {} and file md5 {}", contentMd5, fileMd5);

            needUpdate = !contentMd5.equals(fileMd5);
        } else {
            // New file
            log.debug("Is a new file");
            needUpdate = true;
        }

        // Write the file if needed
        if (needUpdate) {
            log.debug("Creating the file");
            if (!writeFile(content, file)) {
                throw new SmallToolsException("Could not write the file " + path);
            }
        }

        return needUpdate;
    }

    /**
     * Save some texts to a file.
     * 
     * @param path
     *            the path to the file
     * @param content
     *            the text content
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if the file was created or the content is different
     */
    public static boolean writeFileWithContentCheck(String path, String content, String owner, String group, String permissions) {
        boolean needUpdate = writeFileWithContentCheck(path, content);

        // Update owners and permissions
        changeOwnerAndGroup(path, false, owner, group);
        changePermissions(path, false, permissions);

        return needUpdate;
    }

    /**
     * Save some texts to a file.
     * 
     * @param pathParts
     *            the path to the file (e.g new String[] { "/var/vmail/", domain, "/", from, "/Maildir/.Archives" })
     * @param contentLines
     *            all the lines of text
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if the file was created or the content is different
     */
    public static boolean writeFileWithContentCheck(String[] pathParts, List<String> contentLines, String owner, String group, String permissions) {
        return writeFileWithContentCheck(concatPath(pathParts), contentLines, owner, group, permissions);
    }

    /**
     * Save some texts to a file.
     * 
     * @param pathParts
     *            the path to the file (e.g new String[] { "/var/vmail/", domain, "/", from, "/Maildir/.Archives" })
     * @param content
     *            the text content
     * @param owner
     *            the owner of the file
     * @param group
     *            the group of the file
     * @param permissions
     *            the posix permissions of the file ; the numeric permissions (e.g "777")
     * @return true if the file was created or the content is different
     */
    public static boolean writeFileWithContentCheck(String[] pathParts, String content, String owner, String group, String permissions) {
        return writeFileWithContentCheck(concatPath(pathParts), content, owner, group, permissions);
    }
}
