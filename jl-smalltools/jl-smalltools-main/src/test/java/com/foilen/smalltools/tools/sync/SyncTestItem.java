/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.sync;

import com.foilen.smalltools.tools.AbstractBasics;
import com.google.common.collect.ComparisonChain;

public class SyncTestItem extends AbstractBasics implements Comparable<SyncTestItem> {

    private String id;
    private long version;
    private String content;

    public SyncTestItem() {
    }

    public SyncTestItem(String id, long version, String content) {
        this.id = id;
        this.version = version;
        this.content = content;
    }

    @Override
    public int compareTo(SyncTestItem o) {
        return ComparisonChain.start() //
                .compare(this.id, o.id) //
                .compare(this.version, o.version) //
                .compare(this.content, o.content) //
                .result();
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public long getVersion() {
        return version;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}