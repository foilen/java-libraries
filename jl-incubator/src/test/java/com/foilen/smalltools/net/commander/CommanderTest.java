/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

public class CommanderTest {

    static CountDownLatch countDownLatch;

    @Test(timeout = 10000)
    public void testSendACommand() throws Exception {
        // Server
        CommanderServer commanderServer = new CommanderServer();
        commanderServer.start();
        int port = commanderServer.getPort();
        Assert.assertNotEquals(0, port);

        // Client
        countDownLatch = new CountDownLatch(1);
        CommanderClient commanderClient = new CommanderClient();

        // Send one command
        commanderClient.sendCommand("127.0.0.1", port, new CountDownCommand());
        countDownLatch.await();
        Assert.assertEquals(1, commanderClient.getConnectionsCount());

        // Close
        commanderClient.closeConnection("127.0.0.1", port);
        commanderServer.stop();
        Assert.assertEquals(0, commanderClient.getConnectionsCount());
    }

    @Test(timeout = 10000)
    public void testSendACommandWithResponse() throws Exception {
        // Server
        CommanderServer commanderServer = new CommanderServer();
        commanderServer.start();
        int port = commanderServer.getPort();
        Assert.assertNotEquals(0, port);

        // Client
        countDownLatch = new CountDownLatch(1);
        CommanderClient commanderClient = new CommanderClient();

        // Send one command
        String response = commanderClient.sendCommandAndWaitResponse("127.0.0.1", port, new CountDownCommandWithResponse("A"));
        countDownLatch.await();
        Assert.assertEquals(1, commanderClient.getConnectionsCount());
        Assert.assertEquals("AA", response);

        // Close
        commanderClient.closeConnection("127.0.0.1", port);
        commanderServer.stop();
        Assert.assertEquals(0, commanderClient.getConnectionsCount());
    }

    @Test(timeout = 10000)
    public void testSendALotOfCommandsWithResponses() throws Exception {
        // Server
        CommanderServer commanderServer = new CommanderServer();
        commanderServer.start();
        final int port = commanderServer.getPort();
        Assert.assertNotEquals(0, port);

        // Client
        countDownLatch = new CountDownLatch(1);
        final CommanderClient commanderClient = new CommanderClient();

        // Send one command
        String response = commanderClient.sendCommandAndWaitResponse("127.0.0.1", port, new CountDownCommandWithResponse("A"));
        countDownLatch.await();
        Assert.assertEquals("AA", response);

        // Send many commands
        int threads = 100;
        countDownLatch = new CountDownLatch(threads);
        final CountDownLatch startCountDownLatch = new CountDownLatch(threads);
        final CountDownLatch endCountDownLatch = new CountDownLatch(threads);
        final boolean[] gotResponse = new boolean[threads];
        for (int i = 0; i < threads; ++i) {
            final int id = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    startCountDownLatch.countDown();
                    try {
                        startCountDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    String response = commanderClient.sendCommandAndWaitResponse("127.0.0.1", port, new CountDownCommandWithResponse("A"));
                    Assert.assertEquals("AA", response);
                    gotResponse[id] = true;
                    endCountDownLatch.countDown();
                }
            }, "CommanderClient-Sender");
            thread.start();
        }
        countDownLatch.await();
        endCountDownLatch.await();
        Assert.assertEquals(1, commanderClient.getConnectionsCount());

        // Check them all
        int missCount = 0;
        for (int i = 0; i < threads; ++i) {
            if (!gotResponse[i]) {
                ++missCount;
            }
        }
        Assert.assertEquals(0, missCount);

        // Close
        commanderClient.closeConnection("127.0.0.1", port);
        commanderServer.stop();
        Assert.assertEquals(0, commanderClient.getConnectionsCount());
    }

}
