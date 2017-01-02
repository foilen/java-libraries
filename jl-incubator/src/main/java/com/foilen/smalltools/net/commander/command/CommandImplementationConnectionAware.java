/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.command;

import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;

/**
 * Lets the {@link CommandImplementation} know where to send back some commands on the same connection.
 */
public interface CommandImplementationConnectionAware {

    void setCommanderConnection(CommanderConnection commanderConnection);

}
