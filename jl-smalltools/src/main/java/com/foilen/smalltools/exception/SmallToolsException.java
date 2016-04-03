/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.exception;

/**
 * An exception that happens.
 */
public class SmallToolsException extends RuntimeException {
    private static final long serialVersionUID = 201501041L;

    public SmallToolsException(String message) {
        super(message);
    }

    public SmallToolsException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmallToolsException(Throwable cause) {
        super(cause);
    }

}
