/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.hash;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CharsetTools;

/**
 * To create hashes with MD5.
 */
public final class HashMd5sum {

    private static final String ALGORITHM = "MD5";

    /**
     * Take bytes and get its hash.
     *
     * @param in
     *            the content
     * @return the hash in hex
     */
    public static String hashBytes(byte[] in) {
        return HashUtils.hashInputStream(ALGORITHM, new ByteArrayInputStream(in));
    }

    /**
     * Take a file and get its hash.
     *
     * @param file
     *            the file
     * @return the hash of the file in hex
     */
    public static String hashFile(File file) {
        try {
            return HashUtils.hashInputStream(ALGORITHM, new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Take a file and get its hash.
     *
     * @param filePath
     *            the path of the file
     * @return the hash of the file in hex
     */
    public static String hashFile(String filePath) {
        return hashFile(new File(filePath));
    }

    /**
     * Take a {@link String} and get its hash.
     *
     * @param in
     *            the content
     * @return the hash in hex
     */
    public static String hashString(String in) {
        return HashUtils.hashInputStream(ALGORITHM, new ByteArrayInputStream(in.getBytes(CharsetTools.UTF_8)));
    }

    private HashMd5sum() {
    }
}
