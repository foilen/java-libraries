/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools.test.entities;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class MachineStatisticNetwork {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String interfaceName;

    private long inBytes;
    private long outBytes;

    public MachineStatisticNetwork() {
    }

    public MachineStatisticNetwork(String interfaceName, long inBytes, long outBytes) {
        this.interfaceName = interfaceName;
        this.inBytes = inBytes;
        this.outBytes = outBytes;
    }

    public long getInBytes() {
        return inBytes;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public long getOutBytes() {
        return outBytes;
    }

    public void setInBytes(long inBytes) {
        this.inBytes = inBytes;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void setOutBytes(long outBytes) {
        this.outBytes = outBytes;
    }

}
