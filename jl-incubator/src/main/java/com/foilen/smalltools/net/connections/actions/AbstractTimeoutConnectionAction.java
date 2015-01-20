/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.TimeoutHandler;
import com.foilen.smalltools.TimeoutHandler.TimeoutHandlerRunnable;
import com.foilen.smalltools.net.connections.Connection;

/**
 * An implementation of a socket action that needs to be completed in a certain amount of time. For example, sharing a password should be quick, so if it hangs, it might be because the client doesn't
 * know how to talk to this service or you are having a DOS attack.
 */
public abstract class AbstractTimeoutConnectionAction implements ConnectionAction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTimeoutConnectionAction.class);

    protected int negociationTimeoutSeconds = -1;

    @Override
    public Connection executeAction(final Connection connection) {

        if (negociationTimeoutSeconds == -1) {

            // No timeout
            return wrappedExecuteAction(connection);

        } else {

            // With timeout
            TimeoutHandler<Connection> timeoutHandler = new TimeoutHandler<Connection>(negociationTimeoutSeconds * 1000L, new TimeoutHandlerRunnable<Connection>() {

                private Connection result;

                @Override
                public Connection result() {
                    return result;
                }

                @Override
                public void run() {
                    result = wrappedExecuteAction(connection);
                }

                @Override
                public void stopRequested() {
                    connection.close();
                }
            });
            try {
                return timeoutHandler.call();
            } catch (InterruptedException e) {
                logger.info("The action {} timed out", this.getClass().getName());
                return null;
            }
        }

    }

    public int getNegociationTimeoutSeconds() {
        return negociationTimeoutSeconds;
    }

    /**
     * the number of second to execute the full action before the connection gets dropped.
     * 
     * @param negociationTimeoutSeconds
     *            the number of second or -1 for unlimited/disabled
     */
    public void setNegociationTimeoutSeconds(int negociationTimeoutSeconds) {
        this.negociationTimeoutSeconds = negociationTimeoutSeconds;
    }

    /**
     * Instead of implementing {@link #executeAction(Connection)}, the child must implement that one to enable the automatic handling of timeout.
     * 
     * @param connection
     *            the connection to execute on
     * @return the connection or null if it should be dropped out.
     */
    protected abstract Connection wrappedExecuteAction(Connection connection);

}
