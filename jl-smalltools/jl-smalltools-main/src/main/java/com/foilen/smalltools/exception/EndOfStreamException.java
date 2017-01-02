/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.exception;

/**
 * When a stream is EOS when more data was expected.
 */
public class EndOfStreamException extends RuntimeException {
    private static final long serialVersionUID = 20150301201L;

    public EndOfStreamException(String message) {
        super(message);
    }

    public EndOfStreamException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndOfStreamException(Throwable cause) {
        super(cause);
    }
}
