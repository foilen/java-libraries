/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.command;

/**
 * Put this interface on your request data. Your implementation will use {@link CommandImplementation}.
 */
public interface CommandRequest {

    /**
     * Tell what is the full name of the {@link CommandImplementation} that belongs to this request.
     *
     * @return the full name of the implementation class
     */
    String commandImplementationClass();

}
