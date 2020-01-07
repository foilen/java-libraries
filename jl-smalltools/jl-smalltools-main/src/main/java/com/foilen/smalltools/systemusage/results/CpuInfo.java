/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

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

    public long getIdle() {
        return idle;
    }

    public long getIowait() {
        return iowait;
    }

    public long getIrq() {
        return irq;
    }

    public long getNice() {
        return nice;
    }

    public long getSoftirq() {
        return softirq;
    }

    public long getSystem() {
        return system;
    }

    public long getUser() {
        return user;
    }

    public void setIdle(long idle) {
        this.idle = idle;
    }

    public void setIowait(long iowait) {
        this.iowait = iowait;
    }

    public void setIrq(long irq) {
        this.irq = irq;
    }

    public void setNice(long nice) {
        this.nice = nice;
    }

    public void setSoftirq(long softirq) {
        this.softirq = softirq;
    }

    public void setSystem(long system) {
        this.system = system;
    }

    public void setUser(long user) {
        this.user = user;
    }

}
