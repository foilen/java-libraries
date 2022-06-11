/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

import java.util.Optional;

import com.foilen.smalltools.tools.AbstractBasics;

/**
 * A slice to sync.
 *
 * @param <I>
 *            the id type
 */
public class SyncSlice<I> extends AbstractBasics {

    private Optional<I> afterId = Optional.empty();
    private Optional<I> beforeOrEqualId = Optional.empty();

    public Optional<I> getAfterId() {
        return afterId;
    }

    public Optional<I> getBeforeOrEqualId() {
        return beforeOrEqualId;
    }

    public SyncSlice<I> setAfterId(Optional<I> afterId) {
        this.afterId = afterId;
        return this;
    }

    public SyncSlice<I> setBeforeOrEqualId(Optional<I> beforeOrEqualId) {
        this.beforeOrEqualId = beforeOrEqualId;
        return this;
    }

}
