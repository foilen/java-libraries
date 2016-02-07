/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streampair.actions;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.net.SocketsPair;
import com.foilen.smalltools.streampair.StreamPair;
import com.foilen.smalltools.streampair.actions.PasswordGateStreamPairAction;

public class PasswordGateStreamPairActionTest extends AbstractTimeoutStreamPairActionTest {

    private static final String PASSWORD = "myPass";

    private PasswordGateStreamPairAction createAction(String password, int timeout) {
        PasswordGateStreamPairAction action = new PasswordGateStreamPairAction();
        action.setPassword(password);
        action.setNegociationTimeoutSeconds(timeout);
        return action;
    }

    @Test(timeout = 60000)
    public void testExecuteActionNoTimeout() throws InterruptedException, ExecutionException {
        PasswordGateStreamPairAction clientAction = createAction(PASSWORD, -1);
        PasswordGateStreamPairAction serverAction = createAction(PASSWORD, -1);

        SocketsPair socketsPair = new SocketsPair();
        Future<StreamPair> clientFuture = executeAction(new StreamPair(socketsPair.getClient()), clientAction);
        Future<StreamPair> serverFuture = executeAction(new StreamPair(socketsPair.getServer()), serverAction);

        StreamPair clientActual = clientFuture.get();
        StreamPair serverActual = serverFuture.get();

        Assert.assertNotNull(clientActual);
        Assert.assertNotNull(serverActual);
    }

    @Test(timeout = 60000)
    public void testExecuteActionNoTimeoutDifferentPassword() throws InterruptedException, ExecutionException {
        PasswordGateStreamPairAction clientAction = createAction(PASSWORD, -1);
        PasswordGateStreamPairAction serverAction = createAction(PASSWORD + "NOT", -1);

        SocketsPair socketsPair = new SocketsPair();
        Future<StreamPair> clientFuture = executeAction(new StreamPair(socketsPair.getClient()), clientAction);
        Future<StreamPair> serverFuture = executeAction(new StreamPair(socketsPair.getServer()), serverAction);

        StreamPair clientActual = clientFuture.get();
        StreamPair serverActual = serverFuture.get();

        Assert.assertNull(clientActual);
        Assert.assertNull(serverActual);
    }

    @Test(timeout = 60000)
    public void testExecuteActionWithTimeout() throws InterruptedException, ExecutionException {
        PasswordGateStreamPairAction clientAction = createAction(PASSWORD, 10);
        PasswordGateStreamPairAction serverAction = createAction(PASSWORD, 10);

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
        PasswordGateStreamPairAction clientAction = createAction(PASSWORD, 1);

        SocketsPair socketsPair = new SocketsPair();
        Future<StreamPair> clientFuture = executeAction(new StreamPair(socketsPair.getClient()), clientAction);

        StreamPair clientActual = clientFuture.get();

        Assert.assertNull(clientActual);
    }
}
