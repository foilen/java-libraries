/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.systemusage.results;

/**
 * The usage of the memory (via PROC).
 */
public class MemoryInfo {

    private long physicalAvailable;
    private long physicalUsed;
    private long physicalTotal;
    private long swapAvailable;
    private long swapUsed;
    private long swapTotal;

    public long getPhysicalAvailable() {
        return physicalAvailable;
    }

    public long getPhysicalTotal() {
        return physicalTotal;
    }

    public long getPhysicalUsed() {
        return physicalUsed;
    }

    public long getSwapAvailable() {
        return swapAvailable;
    }

    public long getSwapTotal() {
        return swapTotal;
    }

    public long getSwapUsed() {
        return swapUsed;
    }

    public void setPhysicalAvailable(long physicalAvailable) {
        this.physicalAvailable = physicalAvailable;
    }

    public void setPhysicalTotal(long physicalTotal) {
        this.physicalTotal = physicalTotal;
    }

    public void setPhysicalUsed(long physicalUsed) {
        this.physicalUsed = physicalUsed;
    }

    public void setSwapAvailable(long swapAvailable) {
        this.swapAvailable = swapAvailable;
    }

    public void setSwapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
    }

    public void setSwapUsed(long swapUsed) {
        this.swapUsed = swapUsed;
    }

}
