/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander;

import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandRequest;

public class CloseChannelThenReconnectReleaseCommand implements CommandRequest, CommandImplementation {

    @Override
    public String commandImplementationClass() {
        return CloseChannelThenReconnectReleaseCommand.class.getName();
    }

    @Override
    public void run() {
        CloseChannelThenReconnectCommand.release();
    }

}
