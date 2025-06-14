package com.foilen.smalltools.systemusage.implementations;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

/**
 * For Java &lt; 9, uses OperatingSystemMXBean .
 */
public class MemoryUsageOsMxImpl implements MemoryUsageStrategy {

    private OperatingSystemMXBean operatingSystemBean;
    private Method systemFreeMemory;
    private Method systemTotalMemory;

    /**
     * Constructor.
     */
    public MemoryUsageOsMxImpl() {
        operatingSystemBean = ManagementFactory.getOperatingSystemMXBean();

        // Get the methods if present
        Class<?> osbClass = operatingSystemBean.getClass();
        systemFreeMemory = getMethod(osbClass, "getFreePhysicalMemorySize");
        systemTotalMemory = getMethod(osbClass, "getTotalPhysicalMemorySize");
    }

    /**
     * Call the method and return its value or null.
     *
     * @param method the method (also support null)
     * @return its value or null
     */
    private Object callMethod(Method method) {
        try {
            return method.invoke(operatingSystemBean);
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * Return the method or null.
     *
     * @param type the class type
     * @param name the name of the method
     * @return the method or null
     */
    private Method getMethod(Class<?> type, String name) {
        try {
            Method method = type.getDeclaredMethod(name);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
        }

        return null;
    }

    @Override
    public Long getSystemFreeMemory() {
        Object result = callMethod(systemFreeMemory);
        if (result == null) {
            return null;
        }
        return (Long) result;
    }

    @Override
    public Long getSystemTotalMemory() {
        Object result = callMethod(systemTotalMemory);
        if (result == null) {
            return null;
        }
        return (Long) result;
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
