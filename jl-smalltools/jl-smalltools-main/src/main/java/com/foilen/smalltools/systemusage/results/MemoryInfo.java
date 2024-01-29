/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

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

    /**
     * The available physical memory.
     *
     * @return the number of bytes
     */
    public long getPhysicalAvailable() {
        return physicalAvailable;
    }

    /**
     * The total physical memory.
     *
     * @return the number of bytes
     */
    public long getPhysicalTotal() {
        return physicalTotal;
    }

    /**
     * The used physical memory.
     *
     * @return the number of bytes
     */
    public long getPhysicalUsed() {
        return physicalUsed;
    }

    /**
     * The available swap memory.
     *
     * @return the number of bytes
     */
    public long getSwapAvailable() {
        return swapAvailable;
    }

    /**
     * The total swap memory.
     *
     * @return the number of bytes
     */
    public long getSwapTotal() {
        return swapTotal;
    }

    /**
     * The used swap memory.
     *
     * @return the number of bytes
     */
    public long getSwapUsed() {
        return swapUsed;
    }

    /**
     * The available physical memory.
     *
     * @param physicalAvailable the number of bytes
     */
    public void setPhysicalAvailable(long physicalAvailable) {
        this.physicalAvailable = physicalAvailable;
    }

    /**
     * The total physical memory.
     *
     * @param physicalTotal the number of bytes
     */
    public void setPhysicalTotal(long physicalTotal) {
        this.physicalTotal = physicalTotal;
    }

    /**
     * The used physical memory.
     *
     * @param physicalUsed the number of bytes
     */
    public void setPhysicalUsed(long physicalUsed) {
        this.physicalUsed = physicalUsed;
    }

    /**
     * The available swap memory.
     *
     * @param swapAvailable the number of bytes
     */
    public void setSwapAvailable(long swapAvailable) {
        this.swapAvailable = swapAvailable;
    }

    /**
     * The total swap memory.
     *
     * @param swapTotal the number of bytes
     */
    public void setSwapTotal(long swapTotal) {
        this.swapTotal = swapTotal;
    }

    /**
     * The used swap memory.
     *
     * @param swapUsed the number of bytes
     */
    public void setSwapUsed(long swapUsed) {
        this.swapUsed = swapUsed;
    }

}
