/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander;

import java.util.concurrent.Semaphore;

import org.junit.Assert;

import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandImplementationConnectionAware;
import com.foilen.smalltools.net.commander.command.CommandRequest;
import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;

public class CloseChannelThenReconnectCommand implements CommandRequest, CommandImplementation, CommandImplementationConnectionAware {

    private static Semaphore semaphore = new Semaphore(0);
    private static Throwable throwable;

    public static void release() {
        semaphore.release();
    }

    public static void reset() {
        semaphore = new Semaphore(0);
        throwable = null;
    }

    public static Throwable waitForRun() throws InterruptedException {
        semaphore.acquire();
        return throwable;
    }

    private CommanderConnection commanderConnection;

    @Override
    public String commandImplementationClass() {
        return CloseChannelThenReconnectCommand.class.getName();
    }

    @Override
    public void run() {
        try {
            // Close the channel
            Assert.assertTrue(commanderConnection.isConnected());
            commanderConnection.close();
            Assert.assertFalse(commanderConnection.isConnected());

            // Send the release command
            commanderConnection.sendCommand(new CloseChannelThenReconnectReleaseCommand());

        } catch (Throwable e) {
            throwable = e;
            semaphore.release();
        }
    }

    @Override
    public void setCommanderConnection(CommanderConnection commanderConnection) {
        this.commanderConnection = commanderConnection;
    }

}
