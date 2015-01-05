/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.test.exception;

/**
 * An exception that happens in a testing tool.
 */
public class StTestException extends RuntimeException {
    private static final long serialVersionUID = 201501041L;

    public StTestException(String message) {
        super(message);
    }

    public StTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public StTestException(Throwable cause) {
        super(cause);
    }

}
