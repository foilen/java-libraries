/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.command;

/**
 * Extend this class to create a command that sends back a result. The corresponding implementation uses {@link AbstractCommandImplementationWithResponse}.
 * 
 * @param <R>
 *            the response type
 */
public abstract class AbstractCommandRequestWithResponse<R> extends AbstractCommandRequest {

    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}
