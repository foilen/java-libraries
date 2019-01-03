/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.consolerunner;

public class ConsoleKilledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConsoleKilledException() {
        super();
    }

    public ConsoleKilledException(String message) {
        super(message);
    }

    public ConsoleKilledException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsoleKilledException(Throwable cause) {
        super(cause);
    }

}
