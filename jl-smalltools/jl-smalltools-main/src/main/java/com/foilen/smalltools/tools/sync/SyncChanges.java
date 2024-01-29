/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

/**
 * Keep track of the changes that happened during a sync.
 */
public class SyncChanges {

    protected long added;
    protected long updated;
    protected long deleted;

    /**
     * How many were added.
     *
     * @return the count
     */
    public long getAdded() {
        return added;
    }

    /**
     * How many were deleted.
     *
     * @return the count
     */
    public long getDeleted() {
        return deleted;
    }

    /**
     * How many were updated.
     *
     * @return the count
     */
    public long getUpdated() {
        return updated;
    }

    /**
     * If any change happened.
     *
     * @return true if any change happened
     */
    public boolean hasChanged() {
        return added + updated + deleted > 0;
    }

    /**
     * Change the number of added.
     *
     * @param added the number of added
     */
    public void setAdded(long added) {
        this.added = added;
    }

    /**
     * Change the number of deleted.
     *
     * @param deleted the number of deleted
     */
    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }

    /**
     * Change the number of updated.
     *
     * @param updated the number of updated
     */
    public void setUpdated(long updated) {
        this.updated = updated;
    }

}
