/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.exception;

/**
 * When a stream is EOS.
 */
public class EndOfStreamException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private boolean corrupted;

    public EndOfStreamException(boolean corrupted, String message) {
        super(message);
        this.corrupted = corrupted;
    }

    public EndOfStreamException(boolean corrupted, String message, Throwable cause) {
        super(message, cause);
        this.corrupted = corrupted;
    }

    public EndOfStreamException(boolean corrupted, Throwable cause) {
        super(cause);
        this.corrupted = corrupted;
    }

    /**
     * Tells if the end of stream was when more data was expected.
     *
     * @return true if during a partial read ; false if when starting a new read
     */
    public boolean isCorrupted() {
        return corrupted;
    }

}
