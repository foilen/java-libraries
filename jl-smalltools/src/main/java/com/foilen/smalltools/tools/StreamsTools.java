/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.google.common.primitives.Ints;

/**
 * Some simple methods to play with streams.
 */
public final class StreamsTools {

    private static final Logger logger = LoggerFactory.getLogger(StreamsTools.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final int BUFFER_SIZE = 1024;

    /**
     * Take a stream and get it as an array of bytes. The stream is closed at the end.
     * 
     * @param input
     *            the input
     * @return the bytes
     */
    public static byte[] consumeAsBytes(InputStream input) {

        AssertTools.assertNotNull(input, "The input cannot be null");

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            flowStream(input, outputStream);
            return outputStream.toByteArray();
        } finally {
            try {
                input.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Take a stream and get it as a String. The stream is closed at the end.
     * 
     * @param input
     *            the input
     * @return the string
     */
    public static String consumeAsString(InputStream input) {

        AssertTools.assertNotNull(input, "The input cannot be null");

        try {
            Reader reader = new InputStreamReader(input);
            StringBuilder sb = new StringBuilder();

            char[] chars = new char[BUFFER_SIZE];
            int len;
            while ((len = reader.read(chars)) != -1) {
                sb.append(chars, 0, len);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new SmallToolsException("Issue reading the stream", e);
        } finally {
            try {
                input.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Consumes the content of the source, adds it to the destination and closes the source (the destination is still open).
     * 
     * @param source
     *            the stream from where to get the data
     * @param destination
     *            the stream to send the data to
     */
    public static void flowStream(InputStream source, OutputStream destination) {
        flowStream(source, destination, true);
    }

    /**
     * Consumes the content of the source, adds it to the destination.
     * 
     * @param source
     *            the stream from where to get the data
     * @param destination
     *            the stream to send the data to
     * @param closeSource
     *            choose if you want the source to be closed at the end
     */
    public static void flowStream(InputStream source, OutputStream destination, boolean closeSource) {

        AssertTools.assertNotNull(source, "The source cannot be null");
        AssertTools.assertNotNull(destination, "The destination cannot be null");

        try {
            byte[] bytes = new byte[BUFFER_SIZE];
            int len;

            logger.debug("Starting to copy the stream");
            while ((len = source.read(bytes)) != -1) {
                destination.write(bytes, 0, len);

                // Flush if no more bytes available
                if (source.available() == 0) {
                    destination.flush();
                }
            }

            logger.debug("Copy completed");

        } catch (Exception e) {

            logger.error("Issue copying the stream", e);
            throw new SmallToolsException("Issue copying the stream", e);

        } finally {

            // Close the source
            if (closeSource) {
                try {
                    source.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Creates a separate thread to consume the content of the source, add it to the destination and close the source (the destination is still open).
     * 
     * @param source
     *            the stream from where to get the data
     * @param destination
     *            the stream to send the data to
     * @return the thread
     */
    public static Thread flowStreamNonBlocking(final InputStream source, final OutputStream destination) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                flowStream(source, destination);
            }
        });
        thread.start();
        return thread;
    }

    /**
     * Read the length and the content. Needs to be written by {@link #write(OutputStream, byte[])}.
     * 
     * @param source
     *            the input stream
     * @return the content
     */
    public static byte[] readBytes(InputStream source) {
        try {
            // Length
            byte[] lenBytes = new byte[4];
            AssertTools.assertTrue(source.read(lenBytes) == 4, "Could not read the length");
            int len = Ints.fromByteArray(lenBytes);

            // Content
            byte[] content = new byte[len];
            int actualLen = source.read(content);
            AssertTools.assertTrue(actualLen == len, "Didn't read the right amount");
            return content;
        } catch (IOException e) {
            throw new SmallToolsException("Issue reading from the stream", e);
        }
    }

    /**
     * Read the length and the content. Needs to be written by {@link #write(OutputStream, byte[])}. Also verify that the size is not too big since that could eat up all memory.
     * 
     * @param source
     *            the input stream
     * @param maxLength
     *            the max length that we consider as valid
     * @return the content
     */
    public static byte[] readBytes(InputStream source, int maxLength) {
        try {
            // Length
            byte[] lenBytes = new byte[4];
            AssertTools.assertTrue(source.read(lenBytes) == 4, "Could not read the length");
            int len = Ints.fromByteArray(lenBytes);
            AssertTools.assertTrue(len <= maxLength, "The length is bigger than the expected length");
            AssertTools.assertTrue(len > 0, "The length is smaller than 1");

            // Content
            byte[] content = new byte[len];
            int actualLen = source.read(content);
            AssertTools.assertTrue(actualLen == len, "Didn't read the right amount");
            return content;
        } catch (IOException e) {
            throw new SmallToolsException("Issue reading from the stream", e);
        }
    }

    /**
     * Read the value. Needs to be written by {@link #write(OutputStream, int)}.
     * 
     * @param source
     *            the input stream
     * @return the value
     */
    public static int readInt(InputStream source) {
        try {
            // Value
            byte[] value = new byte[4];
            AssertTools.assertTrue(source.read(value) == 4, "Could not read the value");
            return Ints.fromByteArray(value);
        } catch (IOException e) {
            throw new SmallToolsException("Issue reading from the stream", e);
        }
    }

    /**
     * Read the length and the content. Needs to be written by {@link #write(OutputStream, String)}.
     * 
     * @param source
     *            the input stream
     * @return the content
     */
    public static String readString(InputStream source) {
        byte[] bytes = readBytes(source);
        return new String(bytes, UTF8);
    }

    /**
     * Read the length and the content. Needs to be written by {@link #write(OutputStream, String)}.
     * 
     * @param source
     *            the input stream
     * @param maxLength
     *            the max length (in bytes)that we consider as valid
     * @return the content
     */
    public static String readString(InputStream source, int maxLength) {
        byte[] bytes = readBytes(source, maxLength);
        return new String(bytes, UTF8);
    }

    /**
     * Writes the length and the content so that it can be read with {@link #readBytes(InputStream)} without knowing the size.
     * 
     * @param destination
     *            the output stream
     * @param content
     *            what to write
     */
    public static void write(OutputStream destination, byte[] content) {
        try {
            destination.write(Ints.toByteArray(content.length));
            destination.write(content);
            destination.flush();
        } catch (IOException e) {
            throw new SmallToolsException("Issue writing to the stream", e);
        }
    }

    /**
     * Writes the value so that it can be read with {@link #readInt(InputStream)}.
     * 
     * @param destination
     *            the output stream
     * @param value
     *            what to write
     */
    public static void write(OutputStream destination, int value) {
        try {
            destination.write(Ints.toByteArray(value));
            destination.flush();
        } catch (IOException e) {
            throw new SmallToolsException("Issue writing to the stream", e);
        }
    }

    /**
     * Writes the length and the content so that it can be read with {@link #readString(InputStream)} without knowing the size.
     * 
     * @param destination
     *            the output stream
     * @param content
     *            what to write
     */
    public static void write(OutputStream destination, String content) {
        write(destination, content.getBytes(UTF8));
    }

    private StreamsTools() {
    }
}