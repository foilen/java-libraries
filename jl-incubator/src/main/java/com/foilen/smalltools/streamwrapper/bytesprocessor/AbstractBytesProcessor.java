/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streamwrapper.bytesprocessor;

/**
 * Derive some redundant methods from others.
 */
public abstract class AbstractBytesProcessor implements BytesProcessor {

    @Override
    public byte[] process(byte[] content) {
        return process(content, 0, content.length);
    }

}
