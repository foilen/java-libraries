/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;

public class CommanderTest {

    static CountDownLatch countDownLatch;

    @Test(timeout = 60000)
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
        CommanderConnection connection = commanderClient.getCommanderConnection("127.0.0.1", port);
        connection.sendCommand(new CountDownCommand());
        countDownLatch.await();
        Assert.assertEquals(1, commanderClient.getConnectionsCount());

        // Close
        commanderClient.closeConnection("127.0.0.1", port);
        commanderServer.stop();
        Assert.assertEquals(0, commanderClient.getConnectionsCount());
    }

    @Test(timeout = 60000)
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
        CommanderConnection connection = commanderClient.getCommanderConnection("127.0.0.1", port);
        CustomResponse response = connection.sendCommandAndWaitResponse(new CountDownCommandWithResponse("A"));
        countDownLatch.await();
        Assert.assertEquals(1, commanderClient.getConnectionsCount());
        Assert.assertEquals("AA", response.getMsg());

        // Close
        commanderClient.closeConnection("127.0.0.1", port);
        commanderServer.stop();
        Assert.assertEquals(0, commanderClient.getConnectionsCount());
    }

    @Test(timeout = 60000)
    public void testSendALotOfCommandsWithResponses() throws Exception {
        // Server
        CommanderServer commanderServer = new CommanderServer();
        commanderServer.start();
        final int port = commanderServer.getPort();
        Assert.assertNotEquals(0, port);

        // Client
        countDownLatch = new CountDownLatch(1);
        CommanderClient commanderClient = new CommanderClient();

        // Send one command
        final CommanderConnection connection = commanderClient.getCommanderConnection("127.0.0.1", port);
        CustomResponse response = connection.sendCommandAndWaitResponse(new CountDownCommandWithResponse("A"));
        countDownLatch.await();
        Assert.assertEquals("AA", response.getMsg());

        // Send many commands
        int threads = 100;
        countDownLatch = new CountDownLatch(threads);
        final CountDownLatch startCountDownLatch = new CountDownLatch(threads);
        final CountDownLatch endCountDownLatch = new CountDownLatch(threads);
        final boolean[] gotResponse = new boolean[threads];
        for (int i = 0; i < threads; ++i) {
            final int id = i;
            Thread thread = new Thread((Runnable) () -> {
                startCountDownLatch.countDown();
                try {
                    startCountDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                CustomResponse response1 = connection.sendCommandAndWaitResponse(new CountDownCommandWithResponse("A"));
                Assert.assertEquals("AA", response1.getMsg());
                gotResponse[id] = true;
                endCountDownLatch.countDown();
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
