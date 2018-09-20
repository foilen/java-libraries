/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.shell;

import java.io.InputStream;
import java.util.stream.Stream;

public interface ExecResult {

    int getExitCode();

    InputStream getStdErrAsInputStream();

    Stream<String> getStdErrAsLines();

    String getStdErrAsString();

    InputStream getStdOutAsInputStream();

    Stream<String> getStdOutAsLines();

    String getStdOutAsString();

}