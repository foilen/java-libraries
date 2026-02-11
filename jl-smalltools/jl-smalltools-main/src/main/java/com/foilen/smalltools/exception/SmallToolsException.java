package com.foilen.smalltools.exception;

import java.io.Serial;

/**
 * An exception that happens.
 */
public class SmallToolsException extends RuntimeException {
    @Serial
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
