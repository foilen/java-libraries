/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.command.example;

import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.net.commander.command.AbstractCommandWithResponse;

/**
 * Sends a message to the remote server and the remote will append a text to it.
 */
public class HelloCommand extends AbstractCommandWithResponse<String> {

    private String message;

    public HelloCommand() {
    }

    public HelloCommand(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }

    @Override
    protected String runWithResponse() {
        return JavaEnvironmentValues.getHostName() + " : " + message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
