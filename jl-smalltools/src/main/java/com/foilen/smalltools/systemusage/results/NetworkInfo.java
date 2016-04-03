/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.systemusage.results;

/**
 * The usage of one network interface. Warning: this usage is since the start of the interface since it is the cumulative absolute.
 */
public class NetworkInfo {

    private String interfaceName;

    private long inBytes;
    private long inPackets;

    private long outBytes;
    private long outPackets;

    public long getInBytes() {
        return inBytes;
    }

    public long getInPackets() {
        return inPackets;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public long getOutBytes() {
        return outBytes;
    }

    public long getOutPackets() {
        return outPackets;
    }

    public void setInBytes(long inBytes) {
        this.inBytes = inBytes;
    }

    public void setInPackets(long inPackets) {
        this.inPackets = inPackets;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void setOutBytes(long outBytes) {
        this.outBytes = outBytes;
    }

    public void setOutPackets(long outPackets) {
        this.outPackets = outPackets;
    }

}
