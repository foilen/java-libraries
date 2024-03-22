/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.mongodb.distributed.internal;

import com.foilen.smalltools.tools.AbstractBasics;

public class HoldingLockDetails extends AbstractBasics {

    private String threadUniqueId;
    private int reentrantCount;

    private final long dropLockAfter;

    public HoldingLockDetails(String threadUniqueId, long dropLockAfterHeldForTooLongInMs) {
        this.threadUniqueId = threadUniqueId;
        this.reentrantCount = 1;
        this.dropLockAfter = System.currentTimeMillis() + dropLockAfterHeldForTooLongInMs;
    }

    public long incrementReentrantCount() {
        reentrantCount++;
        return reentrantCount;
    }

    public long decrementReentrantCount() {
        reentrantCount--;
        return reentrantCount;
    }

    public String getThreadUniqueId() {
        return threadUniqueId;
    }

    public void setThreadUniqueId(String threadUniqueId) {
        this.threadUniqueId = threadUniqueId;
    }

    public int getReentrantCount() {
        return reentrantCount;
    }

    public void setReentrantCount(int reentrantCount) {
        this.reentrantCount = reentrantCount;
    }

    public long getDropLockAfter() {
        return dropLockAfter;
    }

}
