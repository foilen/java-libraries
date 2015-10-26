package com.foilen.smalltools.net.commander.command;

import io.netty.channel.ChannelHandlerContext;

/**
 * Lets the {@link CommandImplementation} know where to send back some commands on the same channel.
 */
public interface CommandImplementationChannelAware {

    void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext);

}
