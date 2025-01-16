/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.systemusage.implementations;

import com.foilen.smalltools.systemusage.ProcUsage;
import com.foilen.smalltools.systemusage.results.MemoryInfo;

/**
 * For Java &gt; 9 and running on Linux, will read the /proc/ files . Will read max twice per second.
 */
public class MemoryUsageProcImpl implements MemoryUsageStrategy {

    private String procMemPath = "/proc/meminfo";

    private long lastCheck;
    private MemoryInfo lastMemoryInfo;

    /**
     * Default constructor.
     */
    public MemoryUsageProcImpl() {
    }

    /**
     * Constructor with the path to the /proc/meminfo file.
     *
     * @param procMemPath the path to the /proc/meminfo file
     */
    public MemoryUsageProcImpl(String procMemPath) {
        this.procMemPath = procMemPath;
    }

    @Override
    public Long getSystemFreeMemory() {
        refreshIfNeeded();
        return lastMemoryInfo.getPhysicalAvailable();
    }

    @Override
    public Long getSystemTotalMemory() {
        refreshIfNeeded();
        return lastMemoryInfo.getPhysicalTotal();
    }

    @Override
    public Long getSystemUsedMemory() {
        refreshIfNeeded();
        return lastMemoryInfo.getPhysicalUsed();
    }

    private void refreshIfNeeded() {
        if (System.currentTimeMillis() - lastCheck > 500) {
            lastMemoryInfo = ProcUsage.getMemoryInfo(procMemPath);
            lastCheck = System.currentTimeMillis();
        }
    }

}
