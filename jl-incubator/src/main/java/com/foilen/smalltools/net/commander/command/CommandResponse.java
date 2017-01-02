/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.net.commander.connectionpool.GlobalCommanderResponseManager;
import com.foilen.smalltools.tools.JsonTools;

/**
 * A response for {@link AbstractCommandRequestWithResponse}.
 * 
 * @param <R>
 *            the response type
 */
public class CommandResponse<R> implements CommandRequest, CommandImplementation {

    private String requestId;

    // To serialize
    @JsonIgnore
    private R response;

    // To deserialize
    private String responseJson;
    private Class<R> responseType;

    public CommandResponse() {
    }

    public CommandResponse(String requestId, R response) {
        this.requestId = requestId;
        this.response = response;
    }

    @Override
    public String commandImplementationClass() {
        return CommandResponse.class.getName();
    }

    public String getRequestId() {
        return requestId;
    }

    public R getResponse() {
        if (response == null) {
            if (responseType != null && responseJson != null) {
                response = JsonTools.readFromString(responseJson, responseType);
            }
        }
        return response;
    }

    public String getResponseJson() {
        return JsonTools.writeToString(response);
    }

    public String getResponseType() {
        if (response == null) {
            return null;
        }
        return response.getClass().getName();
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

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }

    @SuppressWarnings("unchecked")
    public void setResponseType(String responseType) {
        try {
            this.responseType = (Class<R>) Class.forName(responseType);
        } catch (Exception e) {
            throw new SmallToolsException("Cannot set the class", e);
        }
    }

}
