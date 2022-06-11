/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.internal;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.event.EventList;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.SocketTools;
import com.foilen.smalltools.tools.StreamsTools;

public class FlowStreamThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(FlowStreamThread.class);

    private InputStream source;
    private OutputStream destination;
    private boolean closeAtEnd;

    private EventList<String> completedEventList = new EventList<>();

    private CompletableFuture<Void> future = new CompletableFuture<>();

    public FlowStreamThread(InputStream source, OutputStream destination, boolean closeAtEnd) {
        this.source = source;
        this.destination = destination;
        this.closeAtEnd = closeAtEnd;
    }

    public EventList<String> getCompletedEventList() {
        return completedEventList;
    }

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

    public void setCompletedEventList(EventList<String> completedEventList) {
        this.completedEventList = completedEventList;
    }

}
