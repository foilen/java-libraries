/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander;

import com.foilen.smalltools.net.commander.command.AbstractCommandWithResponse;

public class CountDownCommandWithResponse extends AbstractCommandWithResponse<String> {

    private String msg;

    public CountDownCommandWithResponse() {
    }

    public CountDownCommandWithResponse(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    protected String runWithResponse() {
        CommanderTest.countDownLatch.countDown();
        return msg + msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
