/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper.bytesprocessor;

/**
 * A bytes processor.
 */
public interface BytesProcessor {

    /**
     * Execute the method to modify the content. Must create a new array.
     *
     * @param content
     *            the content to process
     * @return the modified bytes
     */
    byte[] process(byte[] content);

    /**
     * Execute the method to modify the content. Must create a new array.
     *
     * @param content
     *            the content to process
     * @param offset
     *            the offset
     * @param length
     *            the length
     * @return the modified bytes
     */
    byte[] process(byte[] content, int offset, int length);

}
