package com.foilen.smalltools.exception;

/**
 * When a stream is EOS.
 */
public class EndOfStreamException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private boolean corrupted;

    /**
     * Construct.
     *
     * @param corrupted if the stream was corrupted
     * @param message   the message
     */
    public EndOfStreamException(boolean corrupted, String message) {
        super(message);
        this.corrupted = corrupted;
    }

    /**
     * Construct.
     *
     * @param corrupted if the stream was corrupted
     * @param message   the message
     * @param cause     the cause
     */
    public EndOfStreamException(boolean corrupted, String message, Throwable cause) {
        super(message, cause);
        this.corrupted = corrupted;
    }

    /**
     * Construct.
     *
     * @param corrupted if the stream was corrupted
     * @param cause     the cause
     */
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
