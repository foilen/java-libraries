/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander;

import com.foilen.smalltools.net.commander.command.CommandImplementation;
import com.foilen.smalltools.net.commander.command.CommandRequest;

class CountDownCommand implements CommandRequest, CommandImplementation {

    @Override
    public String commandImplementationClass() {
        return CountDownCommand.class.getName();
    }

    @Override
    public void run() {
        CommanderTest.countDownLatch.countDown();
    }
}