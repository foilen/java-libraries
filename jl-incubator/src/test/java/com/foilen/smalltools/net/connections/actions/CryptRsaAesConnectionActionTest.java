/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.net.SocketsPair;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.tools.StreamsTools;

@SuppressWarnings("deprecation")
public class CryptRsaAesConnectionActionTest extends AbstractTimeoutConnectionActionTest {

    private CryptRsaAesConnectionAction createAction(int timeout) {
        CryptRsaAesConnectionAction action = new CryptRsaAesConnectionAction();
        action.setNegociationTimeoutSeconds(timeout);
        action.setAesKeySize(128);
        return action;
    }

    @Test(timeout = 60000)
    public void testExecuteActionNoTimeout() throws InterruptedException, ExecutionException, IOException {
        CryptRsaAesConnectionAction clientAction = createAction(-1);
        CryptRsaAesConnectionAction serverAction = createAction(-1);

        SocketsPair socketsPair = new SocketsPair();
        Future<Connection> clientFuture = executeAction(new Connection(socketsPair.getClient()), clientAction);
        Future<Connection> serverFuture = executeAction(new Connection(socketsPair.getServer()), serverAction);

        Connection clientActual = clientFuture.get();
        Connection serverActual = serverFuture.get();

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
        clientActual.getOutputStream().write(clientToServer.getBytes());
        clientActual.getOutputStream().flush();
        serverActual.getOutputStream().write(serverToClient.getBytes());
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
        CryptRsaAesConnectionAction clientAction = createAction(-1);
        clientAction.setAesKeySize(128);
        CryptRsaAesConnectionAction serverAction = createAction(-1);

        SocketsPair socketsPair = new SocketsPair();
        Future<Connection> clientFuture = executeAction(new Connection(socketsPair.getClient()), clientAction);
        Future<Connection> serverFuture = executeAction(new Connection(socketsPair.getServer()), serverAction);

        Connection clientActual = clientFuture.get();
        Connection serverActual = serverFuture.get();

        Assert.assertNotNull(clientActual);
        Assert.assertNotNull(serverActual);
    }

    @Test(timeout = 60000)
    public void testExecuteActionWithTimeout() throws InterruptedException, ExecutionException {
        CryptRsaAesConnectionAction clientAction = createAction(10);
        CryptRsaAesConnectionAction serverAction = createAction(10);

        SocketsPair socketsPair = new SocketsPair();
        Future<Connection> clientFuture = executeAction(new Connection(socketsPair.getClient()), clientAction);
        Future<Connection> serverFuture = executeAction(new Connection(socketsPair.getServer()), serverAction);

        Connection clientActual = clientFuture.get();
        Connection serverActual = serverFuture.get();

        Assert.assertNotNull(clientActual);
        Assert.assertNotNull(serverActual);
    }

    @Test(timeout = 60000)
    public void testExecuteActionWithTimeoutThatTimedout() throws InterruptedException, ExecutionException {
        CryptRsaAesConnectionAction clientAction = createAction(1);

        SocketsPair socketsPair = new SocketsPair();
        Future<Connection> clientFuture = executeAction(new Connection(socketsPair.getClient()), clientAction);

        Connection clientActual = clientFuture.get();

        Assert.assertNull(clientActual);
    }

}
