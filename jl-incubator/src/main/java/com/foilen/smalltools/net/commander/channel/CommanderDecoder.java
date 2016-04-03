/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.commander.channel;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.tools.JsonTools;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.CharsetUtil;

/**
 * <pre>
 * Decodes this protocol:
 * - classNameSize:int
 * - className:String (must be a {@link Runnable})
 * - jsonContentSize:int
 * - jsonContent:String
 * </pre>
 * 
 * <pre>
 * Dependencies:
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.4.5'
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * </pre>
 */
public class CommanderDecoder extends ReplayingDecoder<Void> {

    private static final Logger logger = LoggerFactory.getLogger(CommanderDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        logger.debug("Trying to decode a message. Readable bytes {}", in.readableBytes());

        // classNameSize:int
        int len = in.readInt();

        // className:String
        String className = in.readBytes(len).toString(CharsetUtil.UTF_8);

        // jsonContentSize:int
        len = in.readInt();

        // jsonContent:String
        String jsonContent = in.readBytes(len).toString(CharsetUtil.UTF_8);

        // Add the JSON runnable to the list
        logger.debug("Decoding class {} with json {}", className, jsonContent);

        out.add(JsonTools.readFromString(jsonContent, Class.forName(className)));
        logger.debug("Completely got a {}", className);
    }

}
