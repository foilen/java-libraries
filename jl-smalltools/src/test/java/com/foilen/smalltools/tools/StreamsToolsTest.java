/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Tests for {@link StreamsTools}.
 */
public class StreamsToolsTest {

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

        verify(outputLogger).info("hello world");
        verify(outputLogger).info("yay");
        verifyNoMoreInteractions(outputLogger);
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

}
