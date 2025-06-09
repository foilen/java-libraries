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

    /**
     * The number of bytes received.
     *
     * @return the number of bytes received
     */
    public long getInBytes() {
        return inBytes;
    }

    /**
     * The number of packets received.
     *
     * @return the number of packets received
     */
    public long getInPackets() {
        return inPackets;
    }

    /**
     * The name of the interface.
     *
     * @return the name of the interface
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * The number of bytes sent.
     *
     * @return the number of bytes sent
     */
    public long getOutBytes() {
        return outBytes;
    }

    /**
     * The number of packets sent.
     *
     * @return the number of packets sent
     */
    public long getOutPackets() {
        return outPackets;
    }

    /**
     * Set the number of bytes received.
     *
     * @param inBytes the number of bytes received
     */
    public void setInBytes(long inBytes) {
        this.inBytes = inBytes;
    }

    /**
     * Set the number of packets received.
     *
     * @param inPackets the number of packets received
     */
    public void setInPackets(long inPackets) {
        this.inPackets = inPackets;
    }

    /**
     * Set the name of the interface.
     *
     * @param interfaceName the name of the interface
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Set the number of bytes sent.
     *
     * @param outBytes the number of bytes sent
     */
    public void setOutBytes(long outBytes) {
        this.outBytes = outBytes;
    }

    /**
     * Set the number of packets sent.
     *
     * @param outPackets the number of packets sent
     */
    public void setOutPackets(long outPackets) {
        this.outPackets = outPackets;
    }

}
