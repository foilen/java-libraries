/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.shell;

import com.foilen.smalltools.iterable.FileLinesIterable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An implementation of {@link ExecResult} that stores the output in memory.
 */
public class ExecResultInMemory implements ExecResult {

    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;
    private int exitCode;

    /**
     * Create an empty result.
     *
     * @param out      the output
     * @param err      the error
     * @param exitCode the exit code
     */
    public ExecResultInMemory(ByteArrayOutputStream out, ByteArrayOutputStream err, int exitCode) {
        this.out = out;
        this.err = err;
        this.exitCode = exitCode;
    }

    /**
     * Get the std error.
     *
     * @return the std error
     */
    public ByteArrayOutputStream getErr() {
        return err;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Get the std output.
     *
     * @return the std output
     */
    public ByteArrayOutputStream getOut() {
        return out;
    }

    @Override
    public InputStream getStdErrAsInputStream() {
        return new ByteArrayInputStream(err.toByteArray());
    }

    @Override
    public Stream<String> getStdErrAsLines() {
        FileLinesIterable linesIterable = new FileLinesIterable();
        linesIterable.openStream(getStdErrAsInputStream());
        return StreamSupport.stream(linesIterable.spliterator(), false);
    }

    @Override
    public String getStdErrAsString() {
        return err.toString();
    }

    @Override
    public InputStream getStdOutAsInputStream() {
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public Stream<String> getStdOutAsLines() {
        FileLinesIterable linesIterable = new FileLinesIterable();
        linesIterable.openStream(getStdOutAsInputStream());
        return StreamSupport.stream(linesIterable.spliterator(), false);
    }

    @Override
    public String getStdOutAsString() {
        return out.toString();
    }

}
