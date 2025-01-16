/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.consolerunner;

/**
 * An exception when the console timed out.
 */
public class ConsoleTimedoutException extends RuntimeException {

    private static final long serialVersionUID = 2015072101L;

    /**
     * Default constructor.
     */
    public ConsoleTimedoutException() {
        super();
    }

    /**
     * Constructor with a message.
     *
     * @param message the message
     */
    public ConsoleTimedoutException(String message) {
        super(message);
    }

    /**
     * Constructor with a message and a cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ConsoleTimedoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with a cause.
     *
     * @param cause the cause
     */
    public ConsoleTimedoutException(Throwable cause) {
        super(cause);
    }

}
