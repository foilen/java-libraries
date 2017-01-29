/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import com.foilen.smalltools.exception.EndOfStreamException;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.base.Charsets;
import com.google.common.primitives.Ints;

/**
 * Tests for {@link StreamsTools}.
 */
public class StreamsToolsTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConsumeAsString() {
        Assert.assertEquals("Hello World", StreamsTools.consumeAsString(getClass().getResourceAsStream("StStreamsToolsTest-file.txt")));
    }

    @Test
    public void testCreateLoggerOutputStream() throws IOException {

        Logger outputLogger = mock(Logger.class);

        OutputStream out = StreamsTools.createLoggerOutputStream(outputLogger, Level.INFO);
        out.write("hello".getBytes());
        out.write(" world\n".getBytes());
        out.write("yay\n".getBytes());

        ThreadTools.sleep(3000);

        verify(outputLogger).info("hello world");
        verify(outputLogger).info("yay");
        verifyNoMoreInteractions(outputLogger);
    }

    @Test(timeout = 30000)
    public void testFillBuffer() throws Throwable {
        Tuple2<PipedInputStream, PipedOutputStream> pipe = StreamsTools.createPipe();
        InputStream in = pipe.getA();
        OutputStream out = pipe.getB();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        Tuple2<Throwable, Void> exception = new Tuple2<>();
        new Thread(() -> {
            try {
                Assert.assertEquals("Hello World", StreamsTools.readString(in));
            } catch (Exception e) {
                exception.setA(e);
            }
            countDownLatch.countDown();
        }).start();

        // Send slowly
        out.write(Ints.toByteArray(11));
        out.write("Hello ".getBytes(Charsets.UTF_8));
        out.flush();

        ThreadTools.sleep(2000);

        out.write("World".getBytes(Charsets.UTF_8));
        out.flush();

        countDownLatch.await();
        if (exception.getA() != null) {
            throw exception.getA();
        }

    }

    @Test
    public void testFlowStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamsTools.flowStream(getClass().getResourceAsStream("StStreamsToolsTest-file.txt"), outputStream);

        Assert.assertEquals("Hello World", StreamsTools.consumeAsString(new ByteArrayInputStream(outputStream.toByteArray())));
    }

    @Test
    public void testFlowStreamNonBlocking() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamsTools.flowStreamNonBlocking(getClass().getResourceAsStream("StStreamsToolsTest-file.txt"), outputStream);

        // Shouldn't have the time to already copy the stream
        Assert.assertNotEquals("Hello World", StreamsTools.consumeAsString(new ByteArrayInputStream(outputStream.toByteArray())));

        // Wait one second and retry
        ThreadTools.sleep(1000);
        Assert.assertEquals("Hello World", StreamsTools.consumeAsString(new ByteArrayInputStream(outputStream.toByteArray())));
    }

    @Test(timeout = 30000)
    public void testWriteAndRead() {
        Tuple2<PipedInputStream, PipedOutputStream> pipe = StreamsTools.createPipe();
        InputStream in = pipe.getA();
        OutputStream out = pipe.getB();

        StreamsTools.write(out, 10);
        StreamsTools.write(out, "Hello World");
        StreamsTools.write(out, "Hello World");

        Assert.assertEquals(10, StreamsTools.readInt(in));
        Assert.assertEquals("Hello World", StreamsTools.readString(in));
        Assert.assertEquals("Hello World", StreamsTools.readString(in, 15));
    }

    @Test(timeout = 30000)
    public void testWriteAndRead_CorruptedContent() throws IOException {

        File tmpFile = File.createTempFile("junit", null);

        OutputStream out = new FileOutputStream(tmpFile);

        StreamsTools.write(out, 10);
        StreamsTools.write(out, "Hello World");
        out.write(Ints.toByteArray(11)); // Len of "Hello World"
        out.write("Hello".getBytes());
        out.close();

        InputStream in = new FileInputStream(tmpFile);
        Assert.assertEquals(10, StreamsTools.readInt(in));
        Assert.assertEquals("Hello World", StreamsTools.readString(in));

        thrown.expect(EndOfStreamException.class);

        StreamsTools.readString(in, 15);

    }

    @Test(timeout = 30000)
    public void testWriteAndRead_CorruptedLength() throws IOException {

        File tmpFile = File.createTempFile("junit", null);

        OutputStream out = new FileOutputStream(tmpFile);

        StreamsTools.write(out, 10);
        StreamsTools.write(out, "Hello World");
        out.write(13);
        out.close();

        InputStream in = new FileInputStream(tmpFile);
        Assert.assertEquals(10, StreamsTools.readInt(in));
        Assert.assertEquals("Hello World", StreamsTools.readString(in));

        thrown.expect(EndOfStreamException.class);

        StreamsTools.readString(in, 15);

    }

    @Test(timeout = 30000)
    public void testWriteAndRead_EOF() throws IOException {

        File tmpFile = File.createTempFile("junit", null);

        OutputStream out = new FileOutputStream(tmpFile);

        StreamsTools.write(out, 10);
        StreamsTools.write(out, "Hello World");
        StreamsTools.write(out, "Hello World");

        InputStream in = new FileInputStream(tmpFile);
        Assert.assertEquals(10, StreamsTools.readInt(in));
        Assert.assertEquals("Hello World", StreamsTools.readString(in));
        Assert.assertEquals("Hello World", StreamsTools.readString(in));

        thrown.expect(EndOfStreamException.class);

        StreamsTools.readString(in);

    }

}
