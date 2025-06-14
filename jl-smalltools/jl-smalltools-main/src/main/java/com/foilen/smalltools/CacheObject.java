package com.foilen.smalltools;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * A single object that is cached for specific duration. The object is generated by a supplier and can be null.
 *
 * @param <T> the type of the object
 */
public class CacheObject<T> {

    private final Duration duration;
    private final Supplier<T> supplier;

    public CacheObject(Duration duration, Supplier<T> supplier) {
        this.duration = duration;
        this.supplier = supplier;
    }

    private AtomicReference<T> objectWrapper;
    private long expireAt;

    /**
     * Get the cached object if present and not expired or generate it.
     *
     * @return the object
     */
    public synchronized T get() {
        // Check if needs to generate
        if (objectWrapper == null || System.currentTimeMillis() > expireAt) {
            if (objectWrapper == null) {
                objectWrapper = new AtomicReference<>();
            }
            objectWrapper.set(supplier.get());
            expireAt = System.currentTimeMillis() + duration.toMillis();
        }

        return objectWrapper.get();
    }

}
