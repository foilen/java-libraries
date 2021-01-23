/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.test.asserts;

import java.util.ArrayList;
import java.util.List;

public class AssertDiff {

    private List<Object> added = new ArrayList<>();
    private List<Object> removed = new ArrayList<>();

    public List<Object> getAdded() {
        return added;
    }

    public List<Object> getRemoved() {
        return removed;
    }

    public void setAdded(List<Object> added) {
        this.added = added;
    }

    public void setRemoved(List<Object> removed) {
        this.removed = removed;
    }

}
