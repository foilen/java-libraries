/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.net.commander.channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.net.commander.command.CommandRequest;
import com.foilen.smalltools.tools.CharsetTools;
import com.foilen.smalltools.tools.JsonTools;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <pre>
 * Encodes this protocol:
 * - classNameSize:int
 * - className:String (must be a {@link CommandRequest})
 * - jsonContentSize:int
 * - jsonContent:String
 * </pre>
 *
 * <pre>
 * Dependencies:
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * compile 'io.netty:netty-all:5.0.0.Alpha2'
 * </pre>
 */
public class CommanderEncoder extends MessageToByteEncoder<CommandRequest> {

    private static final Logger logger = LoggerFactory.getLogger(CommanderEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, CommandRequest msg, ByteBuf out) throws Exception {

        String className = msg.commandImplementationClass();

        logger.debug("Encoding message of type {} to call the implementation {}", msg.getClass().getName(), className);

        try {
            byte[] classNameBytes = className.getBytes(CharsetTools.UTF_8);
            String jsonContent = JsonTools.writeToString(msg);
            byte[] jsonContentBytes = jsonContent.getBytes(CharsetTools.UTF_8);

            out.writeInt(classNameBytes.length);
            out.writeBytes(classNameBytes);

            out.writeInt(jsonContentBytes.length);
            out.writeBytes(jsonContentBytes);
        } catch (Exception e) {
            logger.warn("Problem encoding the message", e);
        }

    }

}
