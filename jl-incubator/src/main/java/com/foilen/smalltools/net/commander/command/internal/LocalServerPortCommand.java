/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.command.internal;

import com.foilen.smalltools.net.commander.CommanderClient;
import com.foilen.smalltools.net.commander.CommanderServer;
import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandImplementationConnectionAware;
import com.foilen.smalltools.net.commander.command.CommandRequest;
import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;

/**
 * This is a command that tells the {@link CommanderServer} how to reconnect to a broken connection. Don't use it directly, simply set {@link CommanderClient#setCommanderServer(CommanderServer)}.
 */
public class LocalServerPortCommand implements CommandRequest, CommandImplementation, CommandImplementationConnectionAware {

    private int port;
    private CommanderConnection commanderConnection;

    public LocalServerPortCommand() {
    }

    public LocalServerPortCommand(int port) {
        this.port = port;
    }

    @Override
    public String commandImplementationClass() {
        return LocalServerPortCommand.class.getName();
    }

    public CommanderConnection getCommanderConnection() {
        return commanderConnection;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        commanderConnection.setPort(port);
    }

    @Override
    public void setCommanderConnection(CommanderConnection commanderConnection) {
        this.commanderConnection = commanderConnection;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
