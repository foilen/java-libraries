/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.net.SocketsPair;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.net.connections.actions.PasswordGateConnectionAction;

public class PasswordGateConnectionActionTest extends AbstractTimeoutConnectionActionTest {

    private static final String PASSWORD = "myPass";

    private PasswordGateConnectionAction createAction(String password, int timeout) {
        PasswordGateConnectionAction action = new PasswordGateConnectionAction();
        action.setPassword(password);
        action.setNegociationTimeoutSeconds(timeout);
        return action;
    }

    @Test(timeout = 10000)
    public void testExecuteActionNoTimeout() throws InterruptedException, ExecutionException {
        PasswordGateConnectionAction clientAction = createAction(PASSWORD, -1);
        PasswordGateConnectionAction serverAction = createAction(PASSWORD, -1);

        SocketsPair socketsPair = new SocketsPair();
        Future<Connection> clientFuture = executeAction(new Connection(socketsPair.getClient()), clientAction);
        Future<Connection> serverFuture = executeAction(new Connection(socketsPair.getServer()), serverAction);

        Connection clientActual = clientFuture.get();
        Connection serverActual = serverFuture.get();

        Assert.assertNotNull(clientActual);
        Assert.assertNotNull(serverActual);
    }

    @Test(timeout = 10000)
    public void testExecuteActionNoTimeoutDifferentPassword() throws InterruptedException, ExecutionException {
        PasswordGateConnectionAction clientAction = createAction(PASSWORD, -1);
        PasswordGateConnectionAction serverAction = createAction(PASSWORD + "NOT", -1);

        SocketsPair socketsPair = new SocketsPair();
        Future<Connection> clientFuture = executeAction(new Connection(socketsPair.getClient()), clientAction);
        Future<Connection> serverFuture = executeAction(new Connection(socketsPair.getServer()), serverAction);

        Connection clientActual = clientFuture.get();
        Connection serverActual = serverFuture.get();

        Assert.assertNull(clientActual);
        Assert.assertNull(serverActual);
    }

    @Test(timeout = 10000)
    public void testExecuteActionWithTimeout() throws InterruptedException, ExecutionException {
        PasswordGateConnectionAction clientAction = createAction(PASSWORD, 10);
        PasswordGateConnectionAction serverAction = createAction(PASSWORD, 10);

        SocketsPair socketsPair = new SocketsPair();
        Future<Connection> clientFuture = executeAction(new Connection(socketsPair.getClient()), clientAction);
        Future<Connection> serverFuture = executeAction(new Connection(socketsPair.getServer()), serverAction);

        Connection clientActual = clientFuture.get();
        Connection serverActual = serverFuture.get();

        Assert.assertNotNull(clientActual);
        Assert.assertNotNull(serverActual);
    }

    @Test(timeout = 10000)
    public void testExecuteActionWithTimeoutThatTimedout() throws InterruptedException, ExecutionException {
        PasswordGateConnectionAction clientAction = createAction(PASSWORD, 1);

        SocketsPair socketsPair = new SocketsPair();
        Future<Connection> clientFuture = executeAction(new Connection(socketsPair.getClient()), clientAction);

        Connection clientActual = clientFuture.get();

        Assert.assertNull(clientActual);
    }
}
