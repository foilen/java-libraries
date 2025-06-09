package com.foilen.smalltools.streamwrapper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Tests for {@link NoneInputStreamWrapper} and {@link NoneOutputStreamWrapper}.
 */
public class NoneStreamWrapperTest extends AbstractStreamWrapperTest {

    @Override
    protected InputStream wrapInputStream(InputStream inputStream) {
        return new NoneInputStreamWrapper(inputStream);
    }

    @Override
    protected OutputStream wrapOutputStream(OutputStream outputStream) {
        return new NoneOutputStreamWrapper(outputStream);
    }
}
