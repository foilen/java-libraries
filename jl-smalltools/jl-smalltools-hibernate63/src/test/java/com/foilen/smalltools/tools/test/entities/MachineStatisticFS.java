/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.test.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
public class MachineStatisticFS {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(length = 2000)
    private String path;
    private boolean isRoot;

    private long usedSpace;
    private long totalSpace;

    public MachineStatisticFS() {
    }

    public MachineStatisticFS(String path, boolean isRoot, long usedSpace, long totalSpace) {
        this.path = path;
        this.isRoot = isRoot;
        this.usedSpace = usedSpace;
        this.totalSpace = totalSpace;
    }

    public String getPath() {
        return path;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRoot(boolean isRoot) {
        this.isRoot = isRoot;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }

}
