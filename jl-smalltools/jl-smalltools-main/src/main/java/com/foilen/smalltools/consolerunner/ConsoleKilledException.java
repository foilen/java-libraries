/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.consolerunner;

/**
 * The console was killed.
 */
public class ConsoleKilledException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * The default constructor.
     */
    public ConsoleKilledException() {
        super();
    }

    /**
     * The constructor with a message.
     *
     * @param message the message
     */
    public ConsoleKilledException(String message) {
        super(message);
    }

    /**
     * The constructor with a message and a cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ConsoleKilledException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * The constructor with a cause.
     *
     * @param cause the cause
     */
    public ConsoleKilledException(Throwable cause) {
        super(cause);
    }

}
