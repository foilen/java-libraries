/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.systemusage.results;

/**
 * The usage of one CPU. Warning: this usage is since the start of the machine since it is the cumulative absolute.
 */
public class CpuInfo {

    private long user;
    private long nice;
    private long system;
    private long idle;
    private long iowait;
    private long irq;
    private long softirq;

    /**
     * Get the total busy usage (all, but idle). Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the busy usage
     */
    public long calculateBusy() {
        return user + nice + system + iowait + irq + softirq;
    }

    /**
     * Get the busy percentage. Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the busy percentage
     */
    public long calculateBusyPercent() {
        return Math.round(calculateBusy() * 100d / calculateTotal());
    }

    /**
     * Get the total usage (busy + idle). Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the total
     */
    public long calculateTotal() {
        return user + nice + system + idle + iowait + irq + softirq;
    }

    /**
     * Get the idle usage. Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the idle
     */
    public long getIdle() {
        return idle;
    }

    /**
     * Get the iowait usage. Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the iowait
     */
    public long getIowait() {
        return iowait;
    }

    /**
     * Get the irq usage. Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the irq
     */
    public long getIrq() {
        return irq;
    }

    /**
     * Get the nice usage. Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the nice
     */
    public long getNice() {
        return nice;
    }

    /**
     * Get the softirq usage. Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the softirq
     */
    public long getSoftirq() {
        return softirq;
    }

    /**
     * Get the system usage. Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the system
     */
    public long getSystem() {
        return system;
    }

    /**
     * Get the user usage. Warning: this usage is since the start of the machine since it is the cumulative absolute.
     *
     * @return the user
     */
    public long getUser() {
        return user;
    }

    /**
     * Set the idle usage
     *
     * @param idle the idle
     */
    public void setIdle(long idle) {
        this.idle = idle;
    }

    /**
     * Set the iowait usage
     *
     * @param iowait the iowait
     */
    public void setIowait(long iowait) {
        this.iowait = iowait;
    }

    /**
     * Set the irq usage
     *
     * @param irq the irq
     */
    public void setIrq(long irq) {
        this.irq = irq;
    }

    /**
     * Set the nice usage
     *
     * @param nice the nice
     */
    public void setNice(long nice) {
        this.nice = nice;
    }

    /**
     * Set the softirq usage
     *
     * @param softirq the softirq
     */
    public void setSoftirq(long softirq) {
        this.softirq = softirq;
    }

    /**
     * Set the system usage
     *
     * @param system the system
     */
    public void setSystem(long system) {
        this.system = system;
    }

    /**
     * Set the user usage
     *
     * @param user the user
     */
    public void setUser(long user) {
        this.user = user;
    }

}
