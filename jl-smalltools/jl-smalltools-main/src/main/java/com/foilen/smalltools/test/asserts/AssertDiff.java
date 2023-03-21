/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.test.asserts;

import java.util.ArrayList;
import java.util.List;

/**
 * The differences.
 */
public class AssertDiff {

    private List<Object> added = new ArrayList<>();
    private List<Object> removed = new ArrayList<>();

    /**
     * Get what was added.
     *
     * @return the added
     */
    public List<Object> getAdded() {
        return added;
    }

    /**
     * Get what was removed.
     *
     * @return the removed
     */
    public List<Object> getRemoved() {
        return removed;
    }

    /**
     * Set what was added.
     *
     * @param added the added
     */
    public void setAdded(List<Object> added) {
        this.added = added;
    }

    /**
     * Set what was removed.
     *
     * @param removed the removed
     */
    public void setRemoved(List<Object> removed) {
        this.removed = removed;
    }

}
