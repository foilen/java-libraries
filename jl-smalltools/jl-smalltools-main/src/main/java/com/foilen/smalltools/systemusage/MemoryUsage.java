/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.systemusage;

import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.systemusage.implementations.MemoryUsageOsMxImpl;
import com.foilen.smalltools.systemusage.implementations.MemoryUsageProcImpl;
import com.foilen.smalltools.systemusage.implementations.MemoryUsageStrategy;

/**
 * To retrieve the usage of the memory of the system.
 */
public class MemoryUsage {

    private static MemoryUsageStrategy memoryUsageStrategy;

    static {

        if (JavaEnvironmentValues.getJavaClassVersion() <= 52) {
            memoryUsageStrategy = new MemoryUsageOsMxImpl();
        } else {
            if (JavaEnvironmentValues.getOperatingSystem().toLowerCase().startsWith("linux")) {
                memoryUsageStrategy = new MemoryUsageProcImpl();
            }
        }

    }

    /**
     * The free memory of the system.
     *
     * @return the free memory of the system or null if cannot get it
     */
    public static Long getSystemFreeMemory() {
        return memoryUsageStrategy.getSystemFreeMemory();
    }

    /**
     * The free memory of the system in percent.
     *
     * @return the free memory of the system in percent or null if cannot get it
     */
    public static Double getSystemFreeMemoryPercent() {
        Long free = getSystemFreeMemory();
        Long total = getSystemTotalMemory();
        if (free == null || total == null || total == 0) {
            return null;
        }
        return free * 100.0 / total;
    }

    /**
     * The total memory of the system.
     *
     * @return the total memory of the system or null if cannot get it
     */
    public static Long getSystemTotalMemory() {
        return memoryUsageStrategy.getSystemTotalMemory();
    }

    /**
     * The used memory of the system.
     *
     * @return the used memory of the system or null if cannot get it
     */
    public static Long getSystemUsedMemory() {
        return memoryUsageStrategy.getSystemUsedMemory();
    }

    /**
     * The used memory of the system in percent.
     *
     * @return the used memory of the system in percent or null if cannot get it
     */
    public static Double getSystemUsedMemoryPercent() {
        Double freePercent = getSystemFreeMemoryPercent();
        if (freePercent == null) {
            return null;
        }
        return 100.0 - freePercent;
    }

    public static void main(String[] args) {
        System.out.println("getSystemFreeMemory: " + getSystemFreeMemory() + " " + getSystemFreeMemoryPercent() + "%");
        System.out.println("getSystemUsedMemory: " + getSystemUsedMemory() + " " + getSystemUsedMemoryPercent() + "%");
        System.out.println("getSystemTotalMemory: " + getSystemTotalMemory());
    }

}
