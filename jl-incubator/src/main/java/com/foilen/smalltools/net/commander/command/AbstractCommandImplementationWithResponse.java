/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.commander.connectionpool.CommanderConnection;

/**
 * Extend this class to create a command that sends back a result.
 *
 * @param <R>
 *            the response type
 */
public abstract class AbstractCommandImplementationWithResponse<R> implements CommandImplementation, CommandImplementationConnectionAware {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected CommanderConnection commanderConnection;

    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    @Override
    public void run() {

        logger.debug("Running command with requestId {}", requestId);

        R response = runWithResponse();
        CommandResponse<R> msg = new CommandResponse<>(requestId, response);
        commanderConnection.sendCommand(msg);

        logger.debug("Giving back the response of requestId {}", requestId);
    }

    /**
     * Overwrite with the method that will return a reply.
     *
     * @return the reply
     */
    protected abstract R runWithResponse();

    @Override
    public void setCommanderConnection(CommanderConnection commanderConnection) {
        this.commanderConnection = commanderConnection;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}
