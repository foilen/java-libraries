/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.shell;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.StreamsTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * The result of an execution. The output and error are in files.
 */
public class ExecResultInFiles implements ExecResult {

    private File out;
    private File err;
    private int exitCode;

    /**
     * Constructor.
     *
     * @param out      the output file
     * @param err      the error file
     * @param exitCode the exit code
     */
    public ExecResultInFiles(File out, File err, int exitCode) {
        this.out = out;
        this.err = err;
        this.exitCode = exitCode;
    }

    /**
     * Get the error file.
     *
     * @return the file
     */
    public File getErr() {
        return err;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Get the output file.
     *
     * @return the file
     */
    public File getOut() {
        return out;
    }

    @Override
    public InputStream getStdErrAsInputStream() {
        try {
            return new FileInputStream(err);
        } catch (FileNotFoundException e) {
            throw new SmallToolsException(e);
        }
    }

    @Override
    public Stream<String> getStdErrAsLines() {
        return FileTools.readFileLinesStream(err);
    }

    @Override
    public String getStdErrAsString() {
        return StreamsTools.consumeAsString(getStdErrAsInputStream());
    }

    @Override
    public InputStream getStdOutAsInputStream() {
        try {
            return new FileInputStream(out);
        } catch (FileNotFoundException e) {
            throw new SmallToolsException(e);
        }
    }

    @Override
    public Stream<String> getStdOutAsLines() {
        return FileTools.readFileLinesStream(out);
    }

    @Override
    public String getStdOutAsString() {
        return StreamsTools.consumeAsString(getStdOutAsInputStream());
    }

}
