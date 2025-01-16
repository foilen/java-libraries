/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.internal;

import com.foilen.smalltools.event.EventList;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.SocketTools;
import com.foilen.smalltools.tools.StreamsTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * A thread to flow a stream.
 */
public class FlowStreamThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(FlowStreamThread.class);

    private InputStream source;
    private OutputStream destination;
    private boolean closeAtEnd;

    private EventList<String> completedEventList = new EventList<>();

    private CompletableFuture<Void> future = new CompletableFuture<>();

    /**
     * Create the flow stream thread.
     *
     * @param source      the source
     * @param destination the destination
     * @param closeAtEnd  if true, will close the source and destination at the end
     */
    public FlowStreamThread(InputStream source, OutputStream destination, boolean closeAtEnd) {
        this.source = source;
        this.destination = destination;
        this.closeAtEnd = closeAtEnd;
    }

    /**
     * Get the event list to know when the flow is completed. The event will be "success", "disconnected" or "error".
     *
     * @return the event list
     */
    public EventList<String> getCompletedEventList() {
        return completedEventList;
    }

    /**
     * Get the future to know when the flow is completed.
     *
     * @return the future
     */
    public Future<Void> getFuture() {
        return future;
    }

    @Override
    public void run() {
        try {
            StreamsTools.flowStream(source, destination);
            completedEventList.dispatch("success");
        } catch (Exception e) {
            if (SocketTools.isADisconnectionException(e)) {
                logger.debug("Disconnected");
                completedEventList.dispatch("disconnected");
            } else {
                completedEventList.dispatch("error");
                logger.error("Problem while streaming", e);
            }
        } finally {
            if (closeAtEnd) {
                CloseableTools.close(source);
                CloseableTools.close(destination);
            }
            future.complete(null);
        }
    }

    /**
     * Set the event list to know when the flow is completed. The event will be "success", "disconnected" or "error".
     *
     * @param completedEventList the event list
     */
    public void setCompletedEventList(EventList<String> completedEventList) {
        this.completedEventList = completedEventList;
    }

}
