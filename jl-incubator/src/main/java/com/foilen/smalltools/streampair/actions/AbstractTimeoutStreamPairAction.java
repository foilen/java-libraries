/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streampair.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.TimeoutHandler;
import com.foilen.smalltools.TimeoutHandler.TimeoutHandlerRunnable;
import com.foilen.smalltools.streampair.StreamPair;

/**
 * An implementation of a socket action that needs to be completed in a certain amount of time. For example, sharing a password should be quick, so if it hangs, it might be because the client doesn't
 * know how to talk to this service or you are having a DOS attack.
 */
public abstract class AbstractTimeoutStreamPairAction implements StreamPairAction {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTimeoutStreamPairAction.class);

    protected int negociationTimeoutSeconds = -1;

    @Override
    public StreamPair executeAction(final StreamPair streamPair) {

        if (negociationTimeoutSeconds == -1) {

            // No timeout
            return wrappedExecuteAction(streamPair);

        } else {

            // With timeout
            TimeoutHandler<StreamPair> timeoutHandler = new TimeoutHandler<StreamPair>(negociationTimeoutSeconds * 1000L, new TimeoutHandlerRunnable<StreamPair>() {

                private StreamPair result;

                @Override
                public StreamPair result() {
                    return result;
                }

                @Override
                public void run() {
                    result = wrappedExecuteAction(streamPair);
                }

                @Override
                public void stopRequested() {
                    streamPair.close();
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
     * Instead of implementing {@link #executeAction(StreamPair)}, the child must implement that one to enable the automatic handling of timeout.
     *
     * @param streamPair
     *            the pair to execute on
     * @return the pair or null if it should be dropped out.
     */
    protected abstract StreamPair wrappedExecuteAction(StreamPair streamPair);

}
