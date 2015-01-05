/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.exception;

/**
 * An exception when there is a problem with input/output.
 */
public class StIOException extends SmallToolsException {

    private static final long serialVersionUID = 201501041L;

    public StIOException(String message) {
        super(message);
    }

    public StIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public StIOException(Throwable cause) {
        super(cause);
    }

}
