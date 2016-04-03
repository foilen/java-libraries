/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.command.example;

import com.foilen.smalltools.net.commander.command.AbstractCommandRequestWithResponse;

/**
 * Sends a message to the remote server and the remote will append a text to it.
 */
public class HelloCommand extends AbstractCommandRequestWithResponse<String> {

    private String message;

    public HelloCommand() {
    }

    public HelloCommand(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
