/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.command;

import com.foilen.smalltools.net.commander.connectionpool.GlobalCommanderResponseManager;

/**
 * A response for {@link AbstractCommandWithResponse}.
 * 
 * @param <R>
 *            the response type
 */
public class CommandResponse<R> implements Runnable {

    private String requestId;
    private R response;

    public CommandResponse() {
    }

    public CommandResponse(String requestId, R response) {
        this.requestId = requestId;
        this.response = response;
    }

    public String getRequestId() {
        return requestId;
    }

    public R getResponse() {
        return response;
    }

    @Override
    public void run() {
        GlobalCommanderResponseManager.storeResponse(this);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setResponse(R response) {
        this.response = response;
    }

}
