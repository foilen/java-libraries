/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.command.example;

/**
 * Displays the message in stdout.
 */
public class EchoCommand implements Runnable {

    private String message;

    public EchoCommand() {
    }

    public EchoCommand(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void run() {
        System.out.println(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
