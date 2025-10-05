package com.foilen.smalltools.systemusage.results;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A snapshot of memory usage at a specific point in time.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(alphabetic = true)
public class MemoryUsageSnapshot {

    // System memory
    private Long systemTotalMemory;
    private Long systemUsedMemory;

    // JVM memory
    private long jvmMaxMemory;
    private long jvmTotalMemory;
    private long jvmFreeMemory;

    public MemoryUsageSnapshot() {
    }

    /**
     * Creates a snapshot with the minimum required information.
     *
     * @param systemTotalMemory the total system memory
     * @param systemUsedMemory  the used system memory
     * @param jvmMaxMemory      the maximum JVM memory
     * @param jvmTotalMemory    the total JVM memory
     * @param jvmFreeMemory     the free JVM memory
     */
    public MemoryUsageSnapshot(
            Long systemTotalMemory, Long systemUsedMemory,
            long jvmMaxMemory, long jvmTotalMemory, long jvmFreeMemory
    ) {
        this.systemTotalMemory = systemTotalMemory;
        this.systemUsedMemory = systemUsedMemory;
        this.jvmMaxMemory = jvmMaxMemory;
        this.jvmTotalMemory = jvmTotalMemory;
        this.jvmFreeMemory = jvmFreeMemory;
    }

    /**
     * The free memory of the system.
     *
     * @return the free memory of the system or null if cannot get it
     */
    public Long getSystemFreeMemory() {
        if (systemTotalMemory == null || systemUsedMemory == null) {
            return null;
        }
        return systemTotalMemory - systemUsedMemory;
    }

    /**
     * The free memory of the system in percent.
     *
     * @return the free memory of the system in percent or null if cannot get it
     */
    public Double getSystemFreeMemoryPercent() {
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
    public Long getSystemTotalMemory() {
        return systemTotalMemory;
    }

    /**
     * The used memory of the system.
     *
     * @return the used memory of the system or null if cannot get it
     */
    public Long getSystemUsedMemory() {
        return systemUsedMemory;
    }

    /**
     * The used memory of the system in percent.
     *
     * @return the used memory of the system in percent or null if cannot get it
     */
    public Double getSystemUsedMemoryPercent() {
        Double freePercent = getSystemFreeMemoryPercent();
        if (freePercent == null) {
            return null;
        }
        return 100.0 - freePercent;
    }

    /**
     * The free memory of the JVM heap.
     *
     * @return the free memory of the JVM heap in bytes
     */
    public long getJvmFreeMemory() {
        return jvmFreeMemory;
    }

    /**
     * The free memory of the JVM heap in percent.
     *
     * @return the free memory of the JVM heap in percent
     */
    public double getJvmFreeMemoryPercent() {
        if (jvmTotalMemory == 0) {
            return 0.0;
        }
        return getJvmFreeMemory() * 100.0 / jvmTotalMemory;
    }

    /**
     * The maximum memory that the JVM will attempt to use.
     *
     * @return the maximum memory of the JVM heap in bytes
     */
    public long getJvmMaxMemory() {
        return jvmMaxMemory;
    }

    /**
     * The total memory currently available to the JVM.
     *
     * @return the total memory currently available to the JVM in bytes
     */
    public long getJvmTotalMemory() {
        return jvmTotalMemory;
    }

    /**
     * The used memory of the JVM heap.
     *
     * @return the used memory of the JVM heap in bytes
     */
    public long getJvmUsedMemory() {
        return jvmTotalMemory - jvmFreeMemory;
    }

    /**
     * The used memory of the JVM heap in percent.
     *
     * @return the used memory of the JVM heap in percent
     */
    public double getJvmUsedMemoryPercent() {
        return 100.0 - getJvmFreeMemoryPercent();
    }

    public void setSystemTotalMemory(Long systemTotalMemory) {
        this.systemTotalMemory = systemTotalMemory;
    }

    public void setSystemUsedMemory(Long systemUsedMemory) {
        this.systemUsedMemory = systemUsedMemory;
    }

    public void setJvmMaxMemory(long jvmMaxMemory) {
        this.jvmMaxMemory = jvmMaxMemory;
    }

    public void setJvmTotalMemory(long jvmTotalMemory) {
        this.jvmTotalMemory = jvmTotalMemory;
    }

    public void setJvmFreeMemory(long jvmFreeMemory) {
        this.jvmFreeMemory = jvmFreeMemory;
    }

}
