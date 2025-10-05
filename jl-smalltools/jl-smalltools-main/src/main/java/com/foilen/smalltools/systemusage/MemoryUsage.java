package com.foilen.smalltools.systemusage;

import com.foilen.smalltools.systemusage.implementations.MemoryUsageOsMxImpl;
import com.foilen.smalltools.systemusage.implementations.MemoryUsageProcImpl;
import com.foilen.smalltools.systemusage.implementations.MemoryUsageStrategy;
import com.foilen.smalltools.systemusage.implementations.MemoryUsageSunOsMxImpl;
import com.foilen.smalltools.systemusage.results.MemoryUsageSnapshot;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.SpaceConverterTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * To retrieve the usage of the memory of the system.
 */
public class MemoryUsage {

    private static final Logger logger = LoggerFactory.getLogger(MemoryUsage.class);

    private static final List<MemoryUsageStrategy> MEMORY_USAGE_STRATEGIES = List.of(
            new MemoryUsageOsMxImpl(),
            new MemoryUsageSunOsMxImpl(),
            new MemoryUsageProcImpl()
    );

    private static MemoryUsageStrategy memoryUsageStrategy;

    static {
        for (MemoryUsageStrategy candidate : MEMORY_USAGE_STRATEGIES) {
            if (candidate.getSystemTotalMemory() != null) {
                memoryUsageStrategy = candidate;
                break;
            }
        }
        if (memoryUsageStrategy == null) {
            memoryUsageStrategy = MEMORY_USAGE_STRATEGIES.stream().findFirst().get();
        }
        logger.info("Using {} for memory usage", memoryUsageStrategy.getClass().getSimpleName());
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

    /**
     * The free memory of the JVM heap.
     *
     * @return the free memory of the JVM heap in bytes
     */
    public static long getJvmFreeMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.freeMemory();
    }

    /**
     * The free memory of the JVM heap in percent.
     *
     * @return the free memory of the JVM heap in percent
     */
    public static double getJvmFreeMemoryPercent() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        if (totalMemory == 0) {
            return 0.0;
        }
        return getJvmFreeMemory() * 100.0 / totalMemory;
    }

    /**
     * The maximum memory that the JVM will attempt to use.
     *
     * @return the maximum memory of the JVM heap in bytes
     */
    public static long getJvmMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * The total memory currently available to the JVM.
     *
     * @return the total memory currently available to the JVM in bytes
     */
    public static long getJvmTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * The used memory of the JVM heap.
     *
     * @return the used memory of the JVM heap in bytes
     */
    public static long getJvmUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * The used memory of the JVM heap in percent.
     *
     * @return the used memory of the JVM heap in percent
     */
    public static double getJvmUsedMemoryPercent() {
        return 100.0 - getJvmFreeMemoryPercent();
    }

    /**
     * Creates a snapshot of the current memory usage.
     *
     * @return a snapshot of the current memory usage
     */
    public static MemoryUsageSnapshot getSnapshot() {
        Runtime runtime = Runtime.getRuntime();
        return new MemoryUsageSnapshot(
                getSystemTotalMemory(),
                getSystemUsedMemory(),
                runtime.maxMemory(),
                runtime.totalMemory(),
                runtime.freeMemory()
        );
    }

    /**
     * To test.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        System.out.println("=== System Memory ===");
        System.out.println("getSystemFreeMemory: " + getSystemFreeMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(getSystemFreeMemory()) + ") " + getSystemFreeMemoryPercent() + "%");
        System.out.println("getSystemUsedMemory: " + getSystemUsedMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(getSystemUsedMemory()) + ") " + getSystemUsedMemoryPercent() + "%");
        System.out.println("getSystemTotalMemory: " + getSystemTotalMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(getSystemTotalMemory()) + ")");

        System.out.println("\n=== JVM Memory ===");
        System.out.println("getJvmFreeMemory: " + getJvmFreeMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(getJvmFreeMemory()) + ") " + getJvmFreeMemoryPercent() + "%");
        System.out.println("getJvmUsedMemory: " + getJvmUsedMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(getJvmUsedMemory()) + ") " + getJvmUsedMemoryPercent() + "%");
        System.out.println("getJvmTotalMemory: " + getJvmTotalMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(getJvmTotalMemory()) + ")");
        System.out.println("getJvmMaxMemory: " + getJvmMaxMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(getJvmMaxMemory()) + ")");

        System.out.println("\n=== Using Snapshot ===");
        var snapshot = getSnapshot();

        System.out.println("=== System Memory (from snapshot) ===");
        System.out.println("getSystemFreeMemory: " + snapshot.getSystemFreeMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(snapshot.getSystemFreeMemory()) + ") " + snapshot.getSystemFreeMemoryPercent() + "%");
        System.out.println("getSystemUsedMemory: " + snapshot.getSystemUsedMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(snapshot.getSystemUsedMemory()) + ") " + snapshot.getSystemUsedMemoryPercent() + "%");
        System.out.println("getSystemTotalMemory: " + snapshot.getSystemTotalMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(snapshot.getSystemTotalMemory()) + ")");

        System.out.println("\n=== JVM Memory (from snapshot) ===");
        System.out.println("getJvmFreeMemory: " + snapshot.getJvmFreeMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(snapshot.getJvmFreeMemory()) + ") " + snapshot.getJvmFreeMemoryPercent() + "%");
        System.out.println("getJvmUsedMemory: " + snapshot.getJvmUsedMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(snapshot.getJvmUsedMemory()) + ") " + snapshot.getJvmUsedMemoryPercent() + "%");
        System.out.println("getJvmTotalMemory: " + snapshot.getJvmTotalMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(snapshot.getJvmTotalMemory()) + ")");
        System.out.println("getJvmMaxMemory: " + snapshot.getJvmMaxMemory() + " (" + SpaceConverterTools.convertToBiggestBUnit(snapshot.getJvmMaxMemory()) + ")");

        System.out.println("\n=== JSON (snapshot) ===");
        System.out.println(JsonTools.prettyPrint(snapshot));

        var clonedSnapshot = JsonTools.clone(snapshot);
        System.out.println("\n=== JSON (cloned snapshot) ===");
        System.out.println(JsonTools.prettyPrint(clonedSnapshot));

    }

}
