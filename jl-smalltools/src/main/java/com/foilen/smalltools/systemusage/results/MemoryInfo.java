/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.systemusage.results;

/**
 * The usage of the memory (via PROC).
 */
public class MemoryInfo {

    private long physicalUsed;
    private long physicalTotal;
    private long swapUsed;
    private long swapTotal;

    public long getPhysicalTotal() {
        return physicalTotal;
    }

    public long getPhysicalUsed() {
        return physicalUsed;
    }

    public long getSwapTotal() {
        return swapTotal;
    }

    public long getSwapUsed() {
        return swapUsed;
    }

    public void setPhysicalTotal(long physicalTotal) {
        this.physicalTotal = physicalTotal;
    }

    public void setPhysicalUsed(long physicalUsed) {
        this.physicalUsed = physicalUsed;
    }

    public void setSwapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
    }

    public void setSwapUsed(long swapUsed) {
        this.swapUsed = swapUsed;
    }

}
