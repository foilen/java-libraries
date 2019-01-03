/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.foilen.smalltools.exception.EndOfStreamException;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.internal.FlowStreamThread;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.primitives.Ints;

/**
 * Some simple methods to play with streams.
 *
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:23.0'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 */
public final class StreamsTools {

    private static final Logger logger = LoggerFactory.getLogger(StreamsTools.class);

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
            CloseableTools.close(input);
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
            Reader reader = new InputStreamReader(input, CharsetTools.UTF_8);
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
            CloseableTools.close(input);
        }
    }

    /**
     * Create an {@link OutputStream} where everything written to it will go to a logger.
     *
     * @param outputLogger
     *            the logger where to send each line
     * @param level
     *            the level to log the output
     * @return the {@link OutputStream}
     */
    public static OutputStream createLoggerOutputStream(Logger outputLogger, Level level) {

        AssertTools.assertNotNull(outputLogger, "The Logger cannot be null");
        AssertTools.assertNotNull(level, "The Level cannot be null");

        // Configure the output
        Tuple2<PipedInputStream, PipedOutputStream> pipe = createPipe();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pipe.getA()));

        new Thread(() -> {

            // Set the thread's name
            ThreadTools.nameThread() //
                    .setSeparator(" - ") //
                    .clear() //
                    .appendText("OutputStream pipe to Logger") //
                    .appendDate() //
                    .change();

            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    switch (level) {
                    case DEBUG:
                        outputLogger.debug(line);
                        break;
                    case ERROR:
                        outputLogger.error(line);
                        break;
                    case INFO:
                        outputLogger.info(line);
                        break;
                    case TRACE:
                        outputLogger.trace(line);
                        break;
                    case WARN:
                        outputLogger.warn(line);
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Error while reading the output stream", e);
            } finally {
                CloseableTools.close(bufferedReader);
            }

        }).start();

        return pipe.getB();
    }

    public static Tuple2<PipedInputStream, PipedOutputStream> createPipe() {
        try {
            PipedInputStream pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
            return new Tuple2<>(pipedInputStream, pipedOutputStream);
        } catch (IOException e) {
            throw new SmallToolsException("Problem creating pipe", e);
        }
    }

    /**
     * Read the stream until the buffer is full.
     *
     * @param source
     *            the input stream
     * @param buffer
     *            the buffer to fill
     * @throws EndOfStreamException
     *             if EOF before completely filling the buffer
     */
    private static void fillBuffer(InputStream source, byte[] buffer) {

        int needed = buffer.length;
        int totalRead = 0;

        try {
            int len = source.read(buffer);
            totalRead += len;
            while (totalRead != needed) {
                logger.debug("Read {} bytes. Total read {} and need {}", len, totalRead, needed);
                if (len == -1) {
                    throw new EndOfStreamException(totalRead >= 0, "End of Stream");
                }
                len = source.read(buffer, totalRead, needed - totalRead);
                totalRead += len;
            }
            logger.debug("Completly read {} bytes.", totalRead);
        } catch (IOException e) {
            throw new SmallToolsException("Issue reading from the stream", e);
        }
    }

    /**
     * Creates a separate thread to consume the content of the source, add it to the destination and close the source and the destination.
     *
     * @param source
     *            the stream from where to get the data
     * @param destination
     *            the stream to send the data to
     * @return the thread
     */
    public static FlowStreamThread flowAndCloseStreamNonBlocking(InputStream source, OutputStream destination) {
        FlowStreamThread thread = new FlowStreamThread(source, destination, true);
        thread.start();
        return thread;
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
            throw new SmallToolsException("Issue copying the stream", e);

        } finally {
            // Close the source
            if (closeSource) {
                CloseableTools.close(source);
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
    public static FlowStreamThread flowStreamNonBlocking(InputStream source, OutputStream destination) {
        FlowStreamThread thread = new FlowStreamThread(source, destination, false);
        thread.start();
        return thread;
    }

    /**
     * Creates a separate thread to consume the content of the source, add it to the destination and close the source.
     *
     * @param source
     *            the stream from where to get the data
     * @param destination
     *            the stream to send the data to
     * @param closeAtEnd
     *            tells if you want the destination to be closed when completed
     * @return the thread
     */
    public static FlowStreamThread flowStreamNonBlocking(InputStream source, OutputStream destination, boolean closeAtEnd) {
        FlowStreamThread thread = new FlowStreamThread(source, destination, closeAtEnd);
        thread.start();
        return thread;
    }

    /**
     * Read the length and the content. Needs to be written by {@link #write(OutputStream, byte[])}.
     *
     * @param source
     *            the input stream
     * @return the content
     * @throws EndOfStreamException
     *             if EOF before completely reading the expected bytes
     */
    public static byte[] readBytes(InputStream source) {
        // Length
        byte[] lenBytes = new byte[4];
        fillBuffer(source, lenBytes);
        int len = Ints.fromByteArray(lenBytes);

        // Content
        byte[] content = new byte[len];
        fillBuffer(source, content);
        return content;
    }

    /**
     * Read the length and the content. Needs to be written by {@link #write(OutputStream, byte[])}. Also verify that the size is not too big since that could eat up all memory.
     *
     * @param source
     *            the input stream
     * @param maxLength
     *            the max length that we consider as valid
     * @return the content
     * @throws EndOfStreamException
     *             if EOF before completely reading the expected bytes
     */
    public static byte[] readBytes(InputStream source, int maxLength) {

        // Length
        byte[] lenBytes = new byte[4];
        fillBuffer(source, lenBytes);
        int len = Ints.fromByteArray(lenBytes);
        AssertTools.assertTrue(len <= maxLength, "The length is bigger than the expected length");
        AssertTools.assertTrue(len > 0, "The length is smaller than 1");

        // Content
        byte[] content = new byte[len];
        try {
            fillBuffer(source, content);
        } catch (EndOfStreamException e) {
            throw new EndOfStreamException(true, e.getMessage());
        }
        return content;
    }

    /**
     * Read the value. Needs to be written by {@link #write(OutputStream, int)}.
     *
     * @param source
     *            the input stream
     * @return the value
     * @throws EndOfStreamException
     *             if EOF before completely reading an int
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
     * @throws EndOfStreamException
     *             if EOF before completely reading the expected text
     */
    public static String readString(InputStream source) {
        byte[] bytes = readBytes(source);
        return new String(bytes, CharsetTools.UTF_8);
    }

    /**
     * Read the length and the content. Needs to be written by {@link #write(OutputStream, String)}.
     *
     * @param source
     *            the input stream
     * @param maxLength
     *            the max length (in bytes) that we consider as valid
     * @return the content
     * @throws EndOfStreamException
     *             if EOF before completely reading the expected text
     */
    public static String readString(InputStream source, int maxLength) {
        byte[] bytes = readBytes(source, maxLength);
        return new String(bytes, CharsetTools.UTF_8);
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
        write(destination, content.getBytes(CharsetTools.UTF_8));
    }

    private StreamsTools() {
    }
}
