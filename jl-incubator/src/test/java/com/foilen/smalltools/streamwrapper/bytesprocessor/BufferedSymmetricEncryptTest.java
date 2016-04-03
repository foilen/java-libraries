/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streamwrapper.bytesprocessor;

import org.junit.Assert;
import org.junit.Test;

import com.foilen.smalltools.streamwrapper.bytesprocessor.BufferedSymmetricEncrypt;
import com.foilen.smalltools.streamwrapper.bytesprocessor.BytesProcessor;

/**
 * Tests for {@link BufferedSymmetricEncrypt}.
 */
public class BufferedSymmetricEncryptTest {

    @Test
    public void testBufferedProcessCompleteBlocks() {

        // Create a 6 bytes long buffer (3*2)
        BufferedSymmetricEncrypt item = new BufferedSymmetricEncrypt(3, 2);

        BytesProcessor processor = new BytesProcessor() {
            @Override
            public byte[] process(byte[] content) {
                byte[] result = new byte[content.length];
                for (int i = 0; i < content.length; ++i) {
                    result[i] = (byte) (content[i] + 10);
                }
                return result;
            }

            @Override
            public byte[] process(byte[] content, int offset, int length) {

                byte[] result = new byte[length];
                for (int i = 0; i < length; ++i) {
                    result[i] = (byte) (content[offset + i] + 10);
                }
                return result;
            }
        };

        // Less than a block
        item.addMoreContent(processor, new byte[] { 1, 2 });
        Assert.assertNull(item.getProcessedBlocks());

        // Fill a bit more
        item.addMoreContent(processor, new byte[] { 3, 4, 5, 6, 7 });
        Assert.assertArrayEquals(new byte[] { 2, 11, 12, 13, 14, 15, 16 }, item.getProcessedBlocks());

        // 2 buffers and reminder
        item.addMoreContent(processor, new byte[] { 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 });
        Assert.assertArrayEquals(new byte[] { 2, 17, 18, 19, 20, 21, 22, 2, 23, 24, 25, 26, 27, 28 }, item.getProcessedBlocks());

        // Partial block
        item.addMoreContent(processor, new byte[] { 0, 0, 0, 21, 22, 23, 24, 25, 26, 27, 0, 0, 0 }, 3, 7);
        Assert.assertArrayEquals(new byte[] { 2, 29, 30, 31, 32, 33, 34 }, item.getProcessedBlocks());

        // Buffered Process Flush
        item.bufferedProcessFlushBlocks(processor);
        Assert.assertArrayEquals(new byte[] { 1, 35, 36, 37 }, item.getProcessedBlocks());
    }

}
