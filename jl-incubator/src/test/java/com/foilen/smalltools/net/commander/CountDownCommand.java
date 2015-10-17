/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander;

class CountDownCommand implements Runnable {
    @Override
    public void run() {
        CommanderTest.countDownLatch.countDown();
    }
}