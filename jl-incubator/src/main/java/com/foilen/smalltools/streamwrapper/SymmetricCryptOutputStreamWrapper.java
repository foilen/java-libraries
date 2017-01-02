/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.streamwrapper;

import java.io.IOException;
import java.io.OutputStream;

import com.foilen.smalltools.crypt.symmetric.AbstractSymmetricCrypt;
import com.foilen.smalltools.crypt.symmetric.SymmetricKey;
import com.foilen.smalltools.streamwrapper.bytesprocessor.BufferedSymmetricEncrypt;
import com.foilen.smalltools.streamwrapper.bytesprocessor.SymmetricEncryptBytesProcessor;

/**
 * A stream wrapper that encrypts what is sent.
 * 
 * It buffers around 64 blocks (or until flushed) and then encrypt them all using padding at the end.
 * 
 * <pre>
 * What is sent over the wire is:
 * - A single byte saying how many blocks (including the padding block) is following
 * - All the blocks
 * </pre>
 */
public class SymmetricCryptOutputStreamWrapper extends AbstractOutputStreamWrapper {

    private static final int AMOUNT_OF_BLOCKS = 64;

    private BufferedSymmetricEncrypt toEncryptBuffer;

    private SymmetricEncryptBytesProcessor bytesProcessor;

    public SymmetricCryptOutputStreamWrapper(OutputStream wrappedOutputStream, AbstractSymmetricCrypt<?> crypt, SymmetricKey key, byte[] iv) {
        super(wrappedOutputStream);

        bytesProcessor = new SymmetricEncryptBytesProcessor(crypt, key, iv);
        toEncryptBuffer = new BufferedSymmetricEncrypt(crypt.getBlockSize(), AMOUNT_OF_BLOCKS);
    }

    @Override
    public void flush() throws IOException {
        toEncryptBuffer.bufferedProcessFlushBlocks(bytesProcessor);
        if (toEncryptBuffer.getProcessedBlocks() != null) {
            wrappedOutputStream.write(toEncryptBuffer.getProcessedBlocks());
        }

        super.flush();
    }

    @Override
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        toEncryptBuffer.addMoreContent(bytesProcessor, b, off, len);
        if (toEncryptBuffer.getProcessedBlocks() != null) {
            wrappedOutputStream.write(toEncryptBuffer.getProcessedBlocks());
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[] { (byte) b }, 0, 1);
    }
}
