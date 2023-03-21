/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.exception;

/**
 * An exception that happens.
 */
public class SmallToolsException extends RuntimeException {
    private static final long serialVersionUID = 201501041L;

    /**
     * Create an exception with a message.
     *
     * @param message the message
     */
    public SmallToolsException(String message) {
        super(message);
    }

    /**
     * Create an exception with a message and a cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    public SmallToolsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Create an exception with a cause.
     *
     * @param cause the cause
     */
    public SmallToolsException(Throwable cause) {
        super(cause);
    }

}
