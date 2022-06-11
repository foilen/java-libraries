/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.shell;

import java.io.InputStream;
import java.util.stream.Stream;

import com.foilen.smalltools.exception.SmallToolsException;

public class ExecResultOnlyExitCode implements ExecResult {

    private int exitCode;

    public ExecResultOnlyExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public InputStream getStdErrAsInputStream() {
        throw new SmallToolsException("stderr not stored");
    }

    @Override
    public Stream<String> getStdErrAsLines() {
        throw new SmallToolsException("stderr not stored");
    }

    @Override
    public String getStdErrAsString() {
        throw new SmallToolsException("stderr not stored");
    }

    @Override
    public InputStream getStdOutAsInputStream() {
        throw new SmallToolsException("stdout not stored");
    }

    @Override
    public Stream<String> getStdOutAsLines() {
        throw new SmallToolsException("stdout not stored");
    }

    @Override
    public String getStdOutAsString() {
        throw new SmallToolsException("stdout not stored");
    }

}
