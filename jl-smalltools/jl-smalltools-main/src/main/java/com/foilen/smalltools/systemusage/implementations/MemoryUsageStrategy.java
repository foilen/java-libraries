/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.systemusage.implementations;

public interface MemoryUsageStrategy {

    Long getSystemFreeMemory();

    Long getSystemTotalMemory();

    Long getSystemUsedMemory();

}
