/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.netty;

import io.netty.channel.ChannelHandler;

public class ChannelHandlerContainer {

    private Class<? extends ChannelHandler> channelHandlerClass;
    private Object[] constructorParams;

    public ChannelHandlerContainer(Class<? extends ChannelHandler> channelHandlerClass, Object[] constructorParams) {
        this.channelHandlerClass = channelHandlerClass;
        this.constructorParams = constructorParams;
    }

    public Class<? extends ChannelHandler> getChannelHandlerClass() {
        return channelHandlerClass;
    }

    public Object[] getConstructorParams() {
        return constructorParams;
    }

}
