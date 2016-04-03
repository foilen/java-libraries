/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streamwrapper;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.crypt.symmetric.AbstractSymmetricCrypt;
import com.foilen.smalltools.crypt.symmetric.SymmetricKey;
import com.foilen.smalltools.streamwrapper.bytesprocessor.BufferedSymmetricEncrypt;
import com.foilen.smalltools.streamwrapper.bytesprocessor.SymmetricDecryptBytesProcessor;

/**
 * A stream wrapper that decrypts what is received.
 */
public class SymmetricDecryptInputStreamWrapper extends AbstractInputStreamWrapper {

    private final static Logger log = LoggerFactory.getLogger(BufferedSymmetricEncrypt.class);

    private byte[] decryptedBuffer;
    private int decryptedBufferPos;

    private byte[] readArray;
    private int blockSize;

    private SymmetricDecryptBytesProcessor bytesProcessor;

    public SymmetricDecryptInputStreamWrapper(InputStream wrappedInputStream, AbstractSymmetricCrypt<?> crypt, SymmetricKey key, byte[] iv) {
        super(wrappedInputStream);

        blockSize = crypt.getBlockSize();
        bytesProcessor = new SymmetricDecryptBytesProcessor(crypt, key, iv);
        readArray = new byte[blockSize * 127];
    }

    @Override
    public int read() throws IOException {
        byte[] data = new byte[1];
        int len = read(data, 0, 1);

        // EOF
        if (len == -1) {
            return -1;
        }

        return data[0];
    }

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {

        int amountRead = 0;
        boolean eof = true;

        int end = off + len;
        while (off < end) {

            // Get from the decrypted buffer
            if (decryptedBuffer != null) {
                int amountTocopy = Math.min(end - off, decryptedBuffer.length - decryptedBufferPos);
                System.arraycopy(decryptedBuffer, decryptedBufferPos, b, off, amountTocopy);
                off += amountTocopy;
                decryptedBufferPos += amountTocopy;
                amountRead += amountTocopy;
                if (decryptedBufferPos >= decryptedBuffer.length) {
                    decryptedBuffer = null;
                }
            }

            // Check if complete
            if (off >= end) {
                break;
            }

            // Check if should send already what is available
            if (amountRead > 0 && wrappedInputStream.available() == 0) {
                break;
            }

            // Get from the stream. Make it wait for the full frame
            byte amountEncryptedBlocks = (byte) wrappedInputStream.read();
            log.debug("amountEncryptedBlocks: {}", amountEncryptedBlocks);
            if (amountEncryptedBlocks == -1) {
                eof = true;
                break;
            }
            int amountReadOnStream = wrappedInputStream.read(readArray, 0, amountEncryptedBlocks * blockSize);
            if (amountReadOnStream == -1) {
                eof = true;
                break;
            }
            decryptedBuffer = bytesProcessor.process(readArray, 0, amountReadOnStream);
            decryptedBufferPos = 0;
        }

        // Check if eof
        if (eof && amountRead == 0) {
            return -1;
        }

        return amountRead;
    }
}
