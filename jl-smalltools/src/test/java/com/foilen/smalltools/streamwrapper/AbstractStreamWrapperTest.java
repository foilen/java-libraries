/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streamwrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.foilen.smalltools.test.asserts.AssertTools;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * To help testing stream wrappers.
 */
public abstract class AbstractStreamWrapperTest {

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected InputStream inputStreamInitial;
    protected OutputStream outputStreamInitial;

    @After
    public void after() throws IOException {
        outputStream.close();
        inputStream.close();
    }

    @Before
    public void before() throws IOException {
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

        inputStream = wrapInputStream(pipedInputStream);
        outputStream = wrapOutputStream(pipedOutputStream);

        inputStreamInitial = pipedInputStream;
        outputStreamInitial = pipedOutputStream;
    }

    /**
     * Try with around 10MB.
     * 
     * @throws IOException
     */
    @Test
    public void testBigFile() throws IOException {
        File initialFile = File.createTempFile("junit-init", ".txt");
        File firstFile = File.createTempFile("junit-first", ".txt");
        File secondFile = File.createTempFile("junit-second", ".txt");

        // Create the initial file
        PrintWriter pw = new PrintWriter(initialFile);
        String text = "Hello to the world. This is a pretty sentence you know";
        for (int y = 0; y < 2; ++y) {
            for (int i = 0; i < 50000; ++i) {
                pw.print(text);
            }
            for (int i = 0; i < 50000; ++i) {
                pw.println(text);
            }
        }
        pw.close();

        // Process it
        OutputStream destination = wrapOutputStream(new FileOutputStream(firstFile));
        StreamsTools.flowStream(new FileInputStream(initialFile), destination);
        destination.close();

        // Reverse
        destination = new FileOutputStream(secondFile);
        StreamsTools.flowStream(wrapInputStream(new FileInputStream(firstFile)), destination);
        destination.close();

        // Compare
        AssertTools.assertFileContent(initialFile, secondFile);
    }

    @Test
    public void testWriteAndReadFullArray() throws IOException {
        byte[] b = "Hello World".getBytes();
        byte[] actual = new byte[1024];
        outputStream.write(b);
        outputStream.flush();
        outputStream.close();
        int len = inputStream.read(actual);
        Assert.assertEquals(b.length, len);
        for (int i = 0; i < len; ++i) {
            Assert.assertEquals(b[i], actual[i]);
        }
    }

    @Test
    public void testWriteAndReadPartial() throws IOException {
        byte[] b = "Hello World".getBytes();
        byte[] actual = new byte[1024];
        outputStream.write(b, 6, 3);
        outputStream.flush();
        outputStream.close();
        int len = inputStream.read(actual);
        Assert.assertEquals(3, len);
        for (int i = 0; i < len; ++i) {
            Assert.assertEquals(b[i + 6], actual[i]);
        }
    }

    @Test
    public void testWriteAndReadSingle() throws IOException {
        outputStream.write(10);
        outputStream.flush();
        outputStream.close();
        Assert.assertEquals(10, inputStream.read());
    }

    /**
     * Create your wrapper around the stream.
     * 
     * @param inputStream
     * @return the wrapper
     */
    protected abstract InputStream wrapInputStream(InputStream inputStream);

    /**
     * Create your wrapper around the stream.
     * 
     * @param outputStream
     * @return the wrapper
     */
    protected abstract OutputStream wrapOutputStream(OutputStream outputStream);

}
