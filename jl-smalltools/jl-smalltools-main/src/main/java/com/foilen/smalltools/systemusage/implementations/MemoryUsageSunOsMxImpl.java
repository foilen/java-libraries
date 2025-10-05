package com.foilen.smalltools.systemusage.implementations;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

/**
 * For Java >= 9 on all OS: use com.sun.management.OperatingSystemMXBean
 * to retrieve physical memory information without relying on /proc.
 */
public class MemoryUsageSunOsMxImpl implements MemoryUsageStrategy {

    private final OperatingSystemMXBean operatingSystemBean;

    public MemoryUsageSunOsMxImpl() {
        OperatingSystemMXBean bean = null;
        try {
            var base = ManagementFactory.getOperatingSystemMXBean();
            if (base instanceof OperatingSystemMXBean) {
                bean = (OperatingSystemMXBean) base;
            }
        } catch (Throwable t) {
            // Ignore; will leave bean as null
        }
        this.operatingSystemBean = bean;
    }

    @Override
    public Long getSystemFreeMemory() {
        if (operatingSystemBean == null) {
            return null;
        }
        try {
            long free = operatingSystemBean.getFreePhysicalMemorySize();
            return free >= 0 ? free : null;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public Long getSystemTotalMemory() {
        if (operatingSystemBean == null) {
            return null;
        }
        try {
            long total = operatingSystemBean.getTotalPhysicalMemorySize();
            return total >= 0 ? total : null;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public Long getSystemUsedMemory() {
        Long free = getSystemFreeMemory();
        Long total = getSystemTotalMemory();
        if (free == null || total == null) {
            return null;
        }
        return total - free;
    }

}
