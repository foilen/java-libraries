/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.hash;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.EncodingTools;

/**
 * The utils for hash algorithms.
 */
public final class HashUtils {

    private static final int BUFFER_SIZE = 1024;

    /**
     * Take an {@link InputStream} and get its hash.
     *
     * @param algorithm
     *            the algorithm to use
     * @param in
     *            the input stream with the content
     * @return the hash in hex
     */
    public static String hashInputStream(String algorithm, InputStream in) {

        // Prepare the consumer
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new SmallToolsException(e);
        }

        // Read the stream
        try {
            byte[] bytes = new byte[BUFFER_SIZE];
            int len;

            while ((len = in.read(bytes)) != -1) {
                messageDigest.update(bytes, 0, len);
            }

        } catch (Exception e) {
            throw new SmallToolsException("Issue hashing the stream", e);
        } finally {

            // Close the source
            try {
                in.close();
            } catch (Exception e) {
            }

        }

        // Get the hash
        byte[] digest = messageDigest.digest();
        return EncodingTools.toHex(digest);
    }

}
