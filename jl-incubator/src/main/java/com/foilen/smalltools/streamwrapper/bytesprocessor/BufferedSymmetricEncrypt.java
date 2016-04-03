/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streamwrapper.bytesprocessor;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.tools.AssertTools;
import com.google.common.primitives.Bytes;

/**
 * This is a buffer for many blocs of data to crypt.
 * 
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:18.0'
 * </pre>
 */
public class BufferedSymmetricEncrypt {

    private final static Logger log = LoggerFactory.getLogger(BufferedSymmetricEncrypt.class);

    private int blockSize;

    private byte[] buffer;
    private int bufferPosition;

    private byte[] moreContent;
    private int moreContentStart;
    private int moreContentEnd;

    private byte[] processedBlocks;

    /**
     * 
     * @param blockSize
     *            how many bytes in a block
     * @param amountOfBlocks
     *            how many blocs to buffer (max 127)
     */
    public BufferedSymmetricEncrypt(int blockSize, int amountOfBlocks) {
        this.blockSize = blockSize;
        buffer = new byte[blockSize * amountOfBlocks];
    }

    /**
     * 
     * @param processor
     *            the bytes processor
     * @param moreContent
     *            the moreContent to set
     * 
     */
    public void addMoreContent(BytesProcessor processor, byte[] moreContent) {
        this.moreContent = moreContent;
        moreContentStart = 0;
        moreContentEnd = moreContent.length - 1;
        bufferedProcessCompleteBlocks(processor);
    }

    /**
     * Set partial more content.
     * 
     * @param processor
     *            the bytes processor
     * @param moreContent
     *            the moreContent to set
     * @param offset
     *            the starting index
     * @param length
     *            the length
     */
    public void addMoreContent(BytesProcessor processor, byte[] moreContent, int offset, int length) {
        this.moreContent = moreContent;
        moreContentStart = offset;
        moreContentEnd = Math.min(moreContent.length - 1, offset + length - 1);
        bufferedProcessCompleteBlocks(processor);
    }

    /**
     * Add more bytes to the buffer and process it if full.
     * 
     * @param processor
     *            the bytes processor
     */
    private void bufferedProcessCompleteBlocks(BytesProcessor processor) {

        AssertTools.assertNotNull(processor, "You need to provide a processor");

        // Clear old values
        processedBlocks = null;

        int amountOfMoreContent = moreContentEnd - moreContentStart + 1;
        boolean canFillBuffer = true;
        while (amountOfMoreContent > 0 && canFillBuffer) {

            // Fill the buffer
            int amountLeftToFill = buffer.length - bufferPosition;
            int fillableAmount = Math.min(amountLeftToFill, amountOfMoreContent);
            System.arraycopy(moreContent, moreContentStart, buffer, bufferPosition, fillableAmount);

            moreContentStart += fillableAmount;
            bufferPosition += fillableAmount;

            // Process if filled
            if (bufferPosition >= buffer.length) {
                processBuffer(processor);
            } else {
                canFillBuffer = false;
            }

            amountOfMoreContent = moreContentEnd - moreContentStart + 1;
        }

        moreContent = null;
    }

    /**
     * Force the processing of the partially filled (if not empty) buffer.
     * 
     * @param processor
     *            the bytes processor
     */
    public void bufferedProcessFlushBlocks(BytesProcessor processor) {

        AssertTools.assertNotNull(processor, "You need to provide a processor");

        // Clear old values
        processedBlocks = null;

        // Check if there is something in the buffer
        if (bufferPosition == 0) {
            return;
        }

        // Process the buffer
        processBuffer(processor);
    }

    protected void processBuffer(BytesProcessor processor) {
        byte[] processed;
        if (bufferPosition == buffer.length) {
            processed = processor.process(buffer);
        } else {
            processed = processor.process(Arrays.copyOf(buffer, bufferPosition));
        }
        byte[] blocksInProcessed = new byte[1];
        blocksInProcessed[0] = (byte) (processed.length / blockSize);
        log.debug("number of blocks: {}", blocksInProcessed[0]);
        if (processedBlocks == null) {
            processedBlocks = Bytes.concat(blocksInProcessed, processed);
        } else {
            processedBlocks = Bytes.concat(processedBlocks, blocksInProcessed, processed);
        }
        bufferPosition = 0;
    }

    /**
     * The parts that were processed.
     * 
     * @return the parts that were processed
     */
    public byte[] getProcessedBlocks() {
        return processedBlocks;
    }
}
