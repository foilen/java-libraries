/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

public class SyncChanges {

    protected long added;
    protected long updated;
    protected long deleted;

    public long getAdded() {
        return added;
    }

    public long getDeleted() {
        return deleted;
    }

    public long getUpdated() {
        return updated;
    }

    public boolean hasChanged() {
        return added + updated + deleted > 0;
    }

    public void setAdded(long added) {
        this.added = added;
    }

    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

}
