/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.command;

/**
 * A basic implementation that automatically link this request to the implementation of the same name + "Impl" appended. <br>
 *
 * E.g.: You could have a request named "com.foilen.api.Ping" and the implementation would be "com.foilen.api.PingImpl".
 */
public abstract class AbstractCommandRequest implements CommandRequest {

    /**
     * Uses the full class name of the current object with "Impl" appended.
     *
     * @return the full class name of the current object with "Impl" appended
     */
    @Override
    public String commandImplementationClass() {
        return this.getClass().getName() + "Impl";
    }

}
