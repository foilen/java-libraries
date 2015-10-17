/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.connectionpool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.commander.command.CommandResponse;
import com.foilen.smalltools.tools.SecureRandomTools;

import io.netty.channel.Channel;

/**
 * This manager knows where each replies must go through across all the {@link ConnectionPool}.
 * 
 * <pre>
 * Dependencies:
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * </pre>
 */
public class GlobalCommanderResponseManager {

    static private final Logger logger = LoggerFactory.getLogger(GlobalCommanderResponseManager.class);

    static private Map<String, Object> responseNotificationByRequestId = new ConcurrentHashMap<>();
    @SuppressWarnings("rawtypes")
    static private Map<String, CommandResponse> responseByRequestId = new ConcurrentHashMap<>();
    static private Map<String, Channel> channelByRequestId = new ConcurrentHashMap<>();

    /**
     * Create a request.
     * 
     * @param channel
     *            the channel to check if it is being closed
     * @return the requestId
     */
    public static String createRequest(Channel channel) {

        // Create
        String requestId = SecureRandomTools.randomHexString(20);
        Object notification = new Object();

        // Store
        channelByRequestId.put(requestId, channel);
        responseNotificationByRequestId.put(requestId, notification);

        return requestId;
    }

    /**
     * The {@link CommandResponse} is storing itself here and notifies the waiting thread.
     * 
     * @param commandResponse
     *            the response
     * @param <R>
     *            the response type
     */
    public static <R> void storeResponse(CommandResponse<R> commandResponse) {
        String requestId = commandResponse.getRequestId();

        Object notifier = responseNotificationByRequestId.remove(requestId);
        if (notifier != null) {
            // Store
            responseByRequestId.put(requestId, commandResponse);

            // Notify
            synchronized (notifier) {
                notifier.notifyAll();
            }

        }
    }

    /**
     * Wait for the request to be completed and get the result.
     * 
     * @param requestId
     *            the request id given by {@link #createRequest(Channel)}
     * @return the result
     */
    @SuppressWarnings("rawtypes")
    public static Object waitAndGetResponse(String requestId) {

        Object notification = responseNotificationByRequestId.get(requestId);
        if (notification == null) {
            throw new SmallToolsException("The request id " + requestId + " is unknown");
        }

        synchronized (notification) {
            try {
                for (;;) {
                    // Wait for the response
                    notification.wait(1000);

                    // Check if got the response
                    CommandResponse response = responseByRequestId.remove(requestId);
                    if (response != null) {
                        return response.getResponse();
                    }

                    // Check if closed
                    Channel channel = channelByRequestId.get(requestId);
                    if (channel.closeFuture().isDone()) {
                        throw new SmallToolsException("The connection was closed while waiting for the response");
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("The wait was interrupted", e);
                return null;
            } finally {
                responseByRequestId.remove(requestId);
                channelByRequestId.remove(requestId);
            }
        }
    }

}
