/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.consolerunner;

public class ConsoleTimedoutException extends RuntimeException {

    private static final long serialVersionUID = 2015072101L;

    public ConsoleTimedoutException() {
        super();
    }

    public ConsoleTimedoutException(String message) {
        super(message);
    }

    public ConsoleTimedoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConsoleTimedoutException(Throwable cause) {
        super(cause);
    }

}
