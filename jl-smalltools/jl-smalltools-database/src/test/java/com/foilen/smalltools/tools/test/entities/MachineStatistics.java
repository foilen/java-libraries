/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.test.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
public class MachineStatistics {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    private Date timestamp;

    private long cpuUsed;
    private long cpuTotal;

    private long memoryUsed;
    private long memoryTotal;

    private long memorySwapUsed;
    private long memorySwapTotal;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<MachineStatisticFS> fs = new TreeSet<>();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<MachineStatisticNetwork> networks = new TreeSet<>();

    // Aggregation
    private int aggregationsForHour = 0;
    private int aggregationsForDay = 0;

    public MachineStatistics() {
    }

    public MachineStatistics(Machine machine, Date timestamp, long cpuUsed, long cpuTotal, long memoryUsed, long memoryTotal, long memorySwapUsed, long memorySwapTotal) {
        this.machine = machine;
        this.timestamp = timestamp;
        this.cpuUsed = cpuUsed;
        this.cpuTotal = cpuTotal;
        this.memoryUsed = memoryUsed;
        this.memoryTotal = memoryTotal;
        this.memorySwapUsed = memorySwapUsed;
        this.memorySwapTotal = memorySwapTotal;
    }

    public int getAggregationsForDay() {
        return aggregationsForDay;
    }

    public int getAggregationsForHour() {
        return aggregationsForHour;
    }

    public long getCpuTotal() {
        return cpuTotal;
    }

    public long getCpuUsed() {
        return cpuUsed;
    }

    public Set<MachineStatisticFS> getFs() {
        return fs;
    }

    public Long getId() {
        return id;
    }

    public Machine getMachine() {
        return machine;
    }

    public long getMemorySwapTotal() {
        return memorySwapTotal;
    }

    public long getMemorySwapUsed() {
        return memorySwapUsed;
    }

    public long getMemoryTotal() {
        return memoryTotal;
    }

    public long getMemoryUsed() {
        return memoryUsed;
    }

    public Set<MachineStatisticNetwork> getNetworks() {
        return networks;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setAggregationsForDay(int aggregationsForDay) {
        this.aggregationsForDay = aggregationsForDay;
    }

    public void setAggregationsForHour(int aggregationsForHour) {
        this.aggregationsForHour = aggregationsForHour;
    }

    public void setCpuTotal(long cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public void setCpuUsed(long cpuUsed) {
        this.cpuUsed = cpuUsed;
    }

    public void setFs(Set<MachineStatisticFS> fs) {
        this.fs = fs;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public void setMemorySwapTotal(long memorySwapTotal) {
        this.memorySwapTotal = memorySwapTotal;
    }

    public void setMemorySwapUsed(long memorySwapUsed) {
        this.memorySwapUsed = memorySwapUsed;
    }

    public void setMemoryTotal(long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public void setMemoryUsed(long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public void setNetworks(Set<MachineStatisticNetwork> networks) {
        this.networks = networks;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
