package com.foilen.smalltools.systemusage.implementations;

/**
 * Some strategies to get the memory usage.
 */
public interface MemoryUsageStrategy {

    /**
     * Get the free memory.
     *
     * @return the free memory
     */
    Long getSystemFreeMemory();

    /**
     * Get the total memory.
     *
     * @return the total memory
     */
    Long getSystemTotalMemory();

    /**
     * Get the used memory.
     *
     * @return the used memory
     */
    Long getSystemUsedMemory();

}
