package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.EndOfStreamException;
import com.foilen.smalltools.tuple.Tuple2;
import com.google.common.primitives.Ints;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;

/**
 * Tests for {@link StreamsTools}.
 */
public class StreamsToolsTest {

    @Test
    public void testConsumeAsString() {
        Assert.assertEquals("Hello World", StreamsTools.consumeAsString(getClass().getResourceAsStream("StreamsToolsTest-file.txt")));
    }

    @Test
    public void testConsumeAsString_UTF8() {
        Assert.assertEquals("L'école de la vie", StreamsTools.consumeAsString(getClass().getResourceAsStream("StreamsToolsTest-testConsumeAsString_UTF8.txt")));
    }

    @Test
    public void testCreateLoggerOutputStream() throws IOException {

        Logger outputLogger = mock(Logger.class);

        OutputStream out = StreamsTools.createLoggerOutputStream(outputLogger, Level.INFO);
        out.write("hello".getBytes(StandardCharsets.UTF_8));
        out.write(" world\n".getBytes(StandardCharsets.UTF_8));
        out.write("yay\n".getBytes(StandardCharsets.UTF_8));

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
        out.write("Hello ".getBytes(StandardCharsets.UTF_8));
        out.flush();

        ThreadTools.sleep(2000);

        out.write("World".getBytes(StandardCharsets.UTF_8));
        out.flush();

        countDownLatch.await();
        if (exception.getA() != null) {
            throw exception.getA();
        }

    }

    @Test
    public void testFlowStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamsTools.flowStream(getClass().getResourceAsStream("StreamsToolsTest-file.txt"), outputStream);

        Assert.assertEquals("Hello World", StreamsTools.consumeAsString(new ByteArrayInputStream(outputStream.toByteArray())));
    }

    @Test
    public void testFlowStreamNonBlocking() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamsTools.flowStreamNonBlocking(getClass().getResourceAsStream("StreamsToolsTest-file.txt"), outputStream);

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
        out.write("Hello".getBytes(StandardCharsets.UTF_8));
        out.close();

        InputStream in = new FileInputStream(tmpFile);
        Assert.assertEquals(10, StreamsTools.readInt(in));
        Assert.assertEquals("Hello World", StreamsTools.readString(in));

        boolean gotException = false;
        try {
            StreamsTools.readString(in, 15);
        } catch (EndOfStreamException e) {
            gotException = true;
            Assert.assertTrue(e.isCorrupted());
        }
        Assert.assertTrue(gotException);

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

        boolean gotException = false;
        try {
            StreamsTools.readString(in, 15);
        } catch (EndOfStreamException e) {
            gotException = true;
            Assert.assertTrue(e.isCorrupted());
        }
        Assert.assertTrue(gotException);

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

        boolean gotException = false;
        try {
            StreamsTools.readString(in);
        } catch (EndOfStreamException e) {
            gotException = true;
            Assert.assertFalse(e.isCorrupted());
        }
        Assert.assertTrue(gotException);

    }

}
