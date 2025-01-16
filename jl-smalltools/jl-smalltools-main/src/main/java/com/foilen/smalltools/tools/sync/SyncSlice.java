/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

import com.foilen.smalltools.tools.AbstractBasics;

import java.util.Optional;

/**
 * A slice to sync.
 *
 * @param <I> the id type
 */
public class SyncSlice<I> extends AbstractBasics {

    private Optional<I> afterId = Optional.empty();
    private Optional<I> beforeOrEqualId = Optional.empty();

    /**
     * Get the after id.
     *
     * @return the after id
     */
    public Optional<I> getAfterId() {
        return afterId;
    }

    /**
     * Get the before or equal id.
     *
     * @return the before or equal id
     */
    public Optional<I> getBeforeOrEqualId() {
        return beforeOrEqualId;
    }

    /**
     * Set the after id.
     *
     * @param afterId the after id
     * @return this
     */
    public SyncSlice<I> setAfterId(Optional<I> afterId) {
        this.afterId = afterId;
        return this;
    }

    /**
     * Set the before or equal id.
     *
     * @param beforeOrEqualId the before or equal id
     * @return this
     */
    public SyncSlice<I> setBeforeOrEqualId(Optional<I> beforeOrEqualId) {
        this.beforeOrEqualId = beforeOrEqualId;
        return this;
    }

}
