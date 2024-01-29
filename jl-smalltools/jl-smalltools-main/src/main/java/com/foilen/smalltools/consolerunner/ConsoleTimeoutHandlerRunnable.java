/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.consolerunner;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.TimeoutHandler.TimeoutHandlerRunnable;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.StreamsTools;
import com.google.common.base.Strings;

class ConsoleTimeoutHandlerRunnable implements TimeoutHandlerRunnable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleTimeoutHandlerRunnable.class);

    private static final AtomicLong nextRunnerId = new AtomicLong();

    private ConsoleRunner consoleRunner;

    private Process process;
    private int statusCode;
    private RuntimeException exceptionThrown;

    public ConsoleTimeoutHandlerRunnable(ConsoleRunner consoleUtils) {
        this.consoleRunner = consoleUtils;
    }

    @Override
    public Integer result() {
        if (exceptionThrown != null) {
            throw exceptionThrown;
        }
        return statusCode;
    }

    @Override
    public void run() {

        long runnerId = nextRunnerId.getAndIncrement();

        // Retrieve all the parameters
        String command = consoleRunner.getCommand();
        String workingDirectory = consoleRunner.getWorkingDirectory();
        List<String> arguments = consoleRunner.getArguments();
        InputStream consoleInput = consoleRunner.getConsoleInput();
        OutputStream consoleOutput = consoleRunner.getConsoleOutput();
        OutputStream consoleError = consoleRunner.getConsoleError();
        boolean closeConsoleOutput = consoleRunner.isCloseConsoleOutput();
        boolean closeConsoleError = consoleRunner.isCloseConsoleError();
        Map<String, String> environments = consoleRunner.getEnvironments();
        boolean overrideEnvironment = consoleRunner.isOverrideEnvironment();
        boolean redirectErrorStream = consoleRunner.isRedirectErrorStream();

        // Check the parameters
        if (Strings.isNullOrEmpty(command)) {
            throw new SmallToolsException("No command is set");
        }

        // Get the full command line
        List<String> fullCommand = new ArrayList<String>();
        fullCommand.add(command);
        if (arguments != null) {
            fullCommand.addAll(arguments);
        }

        // Catch any exception to return it when result() is called
        try {
            try {

                // Run the command
                logger.debug("[{}] Command to run: {}", runnerId, fullCommand);
                ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);

                // Working directory
                if (!Strings.isNullOrEmpty(workingDirectory)) {
                    processBuilder.directory(new File(workingDirectory));
                }

                // Environment
                Map<String, String> subProcEnv = processBuilder.environment();
                if (overrideEnvironment) {
                    subProcEnv.clear();
                }
                subProcEnv.putAll(environments);

                process = processBuilder.start();

                // Setup the streams
                List<Future<Void>> streamFutures = new ArrayList<>();
                processBuilder.redirectErrorStream(redirectErrorStream);
                if (consoleInput == null) {
                    CloseableTools.close(process.getOutputStream());
                } else {
                    streamFutures.add(StreamsTools.flowStreamNonBlocking(consoleInput, process.getOutputStream(), true, "stdin-" + runnerId).getFuture());
                }

                if (consoleOutput == null) {
                    CloseableTools.close(process.getInputStream());
                } else {
                    streamFutures.add(StreamsTools.flowStreamNonBlocking(process.getInputStream(), consoleOutput, false, "stdout-" + runnerId).getFuture());
                }

                if (consoleError == null) {
                    CloseableTools.close(process.getErrorStream());
                } else {
                    if (!redirectErrorStream) {
                        streamFutures.add(StreamsTools.flowStreamNonBlocking(process.getErrorStream(), consoleError, false, "stderr-" + runnerId).getFuture());
                    }
                }

                // Wait for the completion of the process
                process.waitFor();

                // Wait for the completion of the streams
                streamFutures.forEach(f -> {
                    try {
                        f.get();
                    } catch (Exception e) {
                        throw new SmallToolsException("Got an exception while waiting for the completion of the streams", e);
                    }
                });

                // Close the threads if needed
                if (closeConsoleOutput) {
                    CloseableTools.close(consoleOutput);
                }
                if (closeConsoleError) {
                    CloseableTools.close(consoleError);
                }

                // Set the result
                statusCode = process.exitValue();

                // Check it was successful
                if (statusCode == 0) {
                    logger.debug("[{}] Command {} succeeded", runnerId, fullCommand);
                } else {
                    logger.debug("[{}] Command {} failed. Exit code: {}", runnerId, fullCommand, statusCode);
                    // Don't fail to get the result
                }

            } catch (Exception e) {
                logger.debug("[{}] Command {} failed.", runnerId, fullCommand, e);
                throw new SmallToolsException(e);
            }
        } catch (RuntimeException e) {
            exceptionThrown = e;
        }
    }

    @Override
    public void stopRequested() {
        if (process != null) {
            process.destroy();
        }
    }

}
