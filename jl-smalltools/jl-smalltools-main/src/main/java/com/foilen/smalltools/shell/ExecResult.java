package com.foilen.smalltools.shell;

import java.io.InputStream;
import java.util.stream.Stream;

/**
 * The result of an execution.
 */
public interface ExecResult {

    /**
     * Get the exit code.
     *
     * @return the exit code
     */
    int getExitCode();

    /**
     * Get the standard error as an input stream.
     *
     * @return the input stream
     */
    InputStream getStdErrAsInputStream();

    /**
     * Get the standard error as a stream of lines.
     *
     * @return the stream of lines
     */
    Stream<String> getStdErrAsLines();

    /**
     * Get the standard error as a string.
     *
     * @return the string
     */
    String getStdErrAsString();

    /**
     * Get the standard output as an input stream.
     *
     * @return the input stream
     */
    InputStream getStdOutAsInputStream();

    /**
     * Get the standard output as a stream of lines.
     *
     * @return the stream of lines
     */
    Stream<String> getStdOutAsLines();

    /**
     * Get the standard output as a string.
     *
     * @return the string
     */
    String getStdOutAsString();

}