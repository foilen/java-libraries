/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.net.SocketsPair;

/**
 * Tests for {@link SocketsPair}.
 */
public class SocketsPairTest {

    /**
     * Try to send a packet from one socket to the other.
     * 
     * @param source
     * @param destination
     * @throws IOException
     */
    private void assertCommunication(Socket source, Socket destination) throws IOException {
        // Get the streams
        OutputStream outputStream = source.getOutputStream();
        InputStream inputStream = destination.getInputStream();

        // Send a packet
        byte[] expected = new byte[] { 'A', 'B' };
        outputStream.write(expected);

        // Receive the packet
        byte[] actual = new byte[10];
        int len = inputStream.read(actual);

        // Verify
        Assert.assertEquals(expected.length, len);
        for (int i = 0; i < len; ++i) {
            Assert.assertEquals(expected[i], actual[i]);
        }
        for (int i = len; i < actual.length; ++i) {
            Assert.assertEquals(0, actual[i]);
        }

    }

    @Test(timeout = 60000)
    public void test() throws IOException {
        SocketsPair socketsPair = new SocketsPair();

        assertCommunication(socketsPair.getServer(), socketsPair.getClient());
        assertCommunication(socketsPair.getClient(), socketsPair.getServer());
    }

}
