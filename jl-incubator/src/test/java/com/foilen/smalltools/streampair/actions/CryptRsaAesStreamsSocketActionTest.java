/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streampair.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.net.SocketsPair;
import com.foilen.smalltools.streampair.StreamPair;
import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.StreamsTools;

public class CryptRsaAesStreamsSocketActionTest extends AbstractTimeoutStreamPairActionTest {

    private CryptRsaAesStreamsSocketAction createAction(int timeout) {
        CryptRsaAesStreamsSocketAction action = new CryptRsaAesStreamsSocketAction();
        action.setNegociationTimeoutSeconds(timeout);
        action.setAesKeySize(128);
        return action;
    }

    @Test(timeout = 60000)
    public void testExecuteActionNoTimeout() throws InterruptedException, ExecutionException, IOException {
        CryptRsaAesStreamsSocketAction clientAction = createAction(-1);
        CryptRsaAesStreamsSocketAction serverAction = createAction(-1);

        SocketsPair socketsPair = new SocketsPair();
        Future<StreamPair> clientFuture = executeAction(new StreamPair(socketsPair.getClient()), clientAction);
        Future<StreamPair> serverFuture = executeAction(new StreamPair(socketsPair.getServer()), serverAction);

        StreamPair clientActual = clientFuture.get();
        StreamPair serverActual = serverFuture.get();

        Assert.assertNotNull(clientActual);
        Assert.assertNotNull(serverActual);

        // Test data passes
        String clientToServer = "ClientToServer";
        StreamsTools.write(clientActual.getOutputStream(), clientToServer);
        String serverToClient = "ServerToClient";
        StreamsTools.write(serverActual.getOutputStream(), serverToClient);

        String clientToServerActual = StreamsTools.readString(serverActual.getInputStream());
        String serverToClientActual = StreamsTools.readString(clientActual.getInputStream());
        Assert.assertEquals(clientToServer, clientToServerActual);
        Assert.assertEquals(serverToClient, serverToClientActual);

        // Test really crypted
        clientActual.getOutputStream().write(clientToServer.getBytes(CharsetTools.UTF_8));
        clientActual.getOutputStream().flush();
        serverActual.getOutputStream().write(serverToClient.getBytes(CharsetTools.UTF_8));
        serverActual.getOutputStream().flush();

        InputStream serverInitialInputStream = socketsPair.getServer().getInputStream();
        byte[] buffer = new byte[serverInitialInputStream.available()];
        serverInitialInputStream.read(buffer);
        clientToServerActual = new String(buffer);

        InputStream clientInitialInputStream = socketsPair.getClient().getInputStream();
        buffer = new byte[clientInitialInputStream.available()];
        clientInitialInputStream.read(buffer);
        serverToClientActual = new String(buffer);

        Assert.assertNotEquals(clientToServer, clientToServerActual);
        Assert.assertNotEquals(serverToClient, serverToClientActual);
    }

    @Test(timeout = 60000)
    public void testExecuteActionNoTimeoutDifferentKeySize() throws InterruptedException, ExecutionException {
        CryptRsaAesStreamsSocketAction clientAction = createAction(-1);
        clientAction.setAesKeySize(128);
        CryptRsaAesStreamsSocketAction serverAction = createAction(-1);

        SocketsPair socketsPair = new SocketsPair();
        Future<StreamPair> clientFuture = executeAction(new StreamPair(socketsPair.getClient()), clientAction);
        Future<StreamPair> serverFuture = executeAction(new StreamPair(socketsPair.getServer()), serverAction);

        StreamPair clientActual = clientFuture.get();
        StreamPair serverActual = serverFuture.get();

        Assert.assertNotNull(clientActual);
        Assert.assertNotNull(serverActual);
    }

    @Test(timeout = 60000)
    public void testExecuteActionWithTimeout() throws InterruptedException, ExecutionException {
        CryptRsaAesStreamsSocketAction clientAction = createAction(10);
        CryptRsaAesStreamsSocketAction serverAction = createAction(10);

        SocketsPair socketsPair = new SocketsPair();
        Future<StreamPair> clientFuture = executeAction(new StreamPair(socketsPair.getClient()), clientAction);
        Future<StreamPair> serverFuture = executeAction(new StreamPair(socketsPair.getServer()), serverAction);

        StreamPair clientActual = clientFuture.get();
        StreamPair serverActual = serverFuture.get();

        Assert.assertNotNull(clientActual);
        Assert.assertNotNull(serverActual);
    }

    @Test(timeout = 60000)
    public void testExecuteActionWithTimeoutThatTimedout() throws InterruptedException, ExecutionException {
        CryptRsaAesStreamsSocketAction clientAction = createAction(1);

        SocketsPair socketsPair = new SocketsPair();
        Future<StreamPair> clientFuture = executeAction(new StreamPair(socketsPair.getClient()), clientAction);

        StreamPair clientActual = clientFuture.get();

        Assert.assertNull(clientActual);
    }

}
