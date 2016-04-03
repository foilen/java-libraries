/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.systemusage;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

/**
 * To retrieve the usage of the memory of the system.
 */
public class MemoryUsage {

    private static OperatingSystemMXBean operatingSystemBean;
    private static Method systemFreeMemory;
    private static Method systemTotalMemory;

    static {
        operatingSystemBean = ManagementFactory.getOperatingSystemMXBean();

        // Get the methods if present
        Class<?> osbClass = operatingSystemBean.getClass();
        systemFreeMemory = getMethod(osbClass, "getFreePhysicalMemorySize");
        systemTotalMemory = getMethod(osbClass, "getTotalPhysicalMemorySize");

    }

    /**
     * Call the method and return its value or null.
     * 
     * @param method
     *            the method (also support null)
     * @return its value or null
     */
    private static Object callMethod(Method method) {
        try {
            return method.invoke(operatingSystemBean);
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * Return the method or null.
     * 
     * @param type
     *            the class type
     * @param name
     *            the name of the method
     * @return the method or null
     */
    private static Method getMethod(Class<?> type, String name) {
        try {
            Method method = type.getDeclaredMethod(name);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
        }

        return null;
    }

    /**
     * The free memory of the system.
     * 
     * @return the free memory of the system or null if cannot get it
     */
    public static Long getSystemFreeMemory() {
        Object result = callMethod(systemFreeMemory);
        if (result == null) {
            return null;
        }
        return (Long) result;
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
        Object result = callMethod(systemTotalMemory);
        if (result == null) {
            return null;
        }
        return (Long) result;
    }

    public static void main(String[] args) {
        System.out.println("getSystemFreeMemory: " + getSystemFreeMemory() + " " + getSystemFreeMemoryPercent() + "%");
        System.out.println("getSystemTotalMemory: " + getSystemTotalMemory());
    }

}
