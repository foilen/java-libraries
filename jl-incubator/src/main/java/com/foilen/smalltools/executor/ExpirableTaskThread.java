/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.executor;

import java.util.concurrent.Semaphore;

@Deprecated
public class ExpirableTaskThread extends Thread {

    private long timeoutThreadMs;
    private GradualThreadsExecutor gradualThreadsExecutor;

    private Semaphore changingState = new Semaphore(1);
    private Runnable task;
    private boolean expired = false;

    public ExpirableTaskThread(int currentNumber, long timeoutThreadMs, GradualThreadsExecutor gradualThreadsExecutor, Runnable task) {
        super("expirable-" + currentNumber);

        this.timeoutThreadMs = timeoutThreadMs;
        this.gradualThreadsExecutor = gradualThreadsExecutor;
        this.task = task;
    }

    @Override
    public void run() {

        while (true) {

            // Execute the task
            task.run();
            task = null;

            // Tell it is free
            gradualThreadsExecutor.nowFree(this);

            // Wait for the expiration
            try {
                Thread.sleep(timeoutThreadMs);
                changingState.acquire();
                expired = task == null;
                if (expired) {
                    gradualThreadsExecutor.expired(this);
                    break;
                }
            } catch (InterruptedException e) {
            } finally {
                changingState.release();
            }

        }

    }

    public boolean setTask(Runnable task) {
        try {
            changingState.acquire();
            if (expired) {
                return false;
            }
            this.task = task;
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            changingState.release();
        }

    }
}
