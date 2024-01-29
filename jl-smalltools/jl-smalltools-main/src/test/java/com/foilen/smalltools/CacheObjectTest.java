/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools;

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class CacheObjectTest {

    @Test
    public void testGet_returnsSuppliedObject() {
        String expected = "Hello, World!";
        CacheObject<String> cacheObject = new CacheObject<>(Duration.ofMillis(1000), () -> expected);

        String actual = cacheObject.get();

        assertEquals(expected, actual);
    }

    @Test
    public void testGet_returnsSameObjectWithinDuration() {
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        CacheObject<Integer> cacheObject = new CacheObject<>(Duration.ofMillis(1000), () -> counter.getAndSet(counter.get() + 1));

        Integer first = cacheObject.get();
        Integer second = cacheObject.get();

        assertEquals(first, second);
    }

    @Test
    public void testGet_returnsNewObjectAfterDuration() throws InterruptedException {
        AtomicReference<Integer> counter = new AtomicReference<>(0);
        CacheObject<Integer> cacheObject = new CacheObject<>(Duration.ofMillis(1000), () -> counter.getAndSet(counter.get() + 1));

        Integer first = cacheObject.get();
        Thread.sleep(1001);
        Integer second = cacheObject.get();

        assertNotEquals(first, second);
    }

    @Test
    public void testGet_returnsNullWhenSupplierReturnsNull() {
        CacheObject<String> cacheObject = new CacheObject<>(Duration.ofMillis(1000), () -> null);

        String actual = cacheObject.get();

        assertNull(actual);
    }

}