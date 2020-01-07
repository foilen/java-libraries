/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.services;

import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * When a new socket is accepted in {@link TCPServerService}, it is sent right away to the {@link SocketCallback}. If you want to use different threads for every incoming connections, use this
 * wrapper.
 *
 * Usage: new TCPServerService(new ExecutorWrappedSocketCallback(yourCallBack)}
 */
public class ExecutorWrappedSocketCallback implements SocketCallback {

    private class DelayedCallback implements Runnable {

        private Socket socket;

        public DelayedCallback(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            socketCallback.newClient(socket);
        }

    }

    private Executor executor = new ThreadPoolExecutor(0, 1000, 1, TimeUnit.MINUTES, new SynchronousQueue<Runnable>());
    private SocketCallback socketCallback;

    public ExecutorWrappedSocketCallback() {
    }

    public ExecutorWrappedSocketCallback(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
    }

    @Override
    public void newClient(Socket socket) {
        executor.execute(new DelayedCallback(socket));
    }

    public void setSocketCallback(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
    }

}
