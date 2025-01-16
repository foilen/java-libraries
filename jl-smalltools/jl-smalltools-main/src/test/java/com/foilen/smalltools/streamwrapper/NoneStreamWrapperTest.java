/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
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
