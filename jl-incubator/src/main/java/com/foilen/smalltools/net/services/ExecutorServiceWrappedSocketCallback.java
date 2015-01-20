/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.services;

import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * When a new socket is accepted in {@link TCPServerService}, it is sent right away to the {@link SocketCallback}. If you want to use different threads for every incoming connections, use this
 * wrapper.
 * 
 * Usage: new TCPServerService(new ExecutorServiceWrappedSocketCallback(yourCallBack)}
 */
public class ExecutorServiceWrappedSocketCallback implements SocketCallback {

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

    private ExecutorService executorService = new ThreadPoolExecutor(1, 10, 20L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(200));
    private SocketCallback socketCallback;

    public ExecutorServiceWrappedSocketCallback() {
    }

    public ExecutorServiceWrappedSocketCallback(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
    }

    @Override
    public void newClient(Socket socket) {
        executorService.execute(new DelayedCallback(socket));
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setSocketCallback(SocketCallback socketCallback) {
        this.socketCallback = socketCallback;
    }

}
