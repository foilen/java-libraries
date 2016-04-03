/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link StreamsTools}.
 */
public class StreamsToolsTest {

    @Test
    public void testConsumeAsString() {
        Assert.assertEquals("Hello World", StreamsTools.consumeAsString(getClass().getResourceAsStream("StStreamsToolsTest-file.txt")));
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
