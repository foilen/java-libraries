/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some tools to execute commands in the console.
 */
public final class ConsoleTools {

    /**
     * Execute the command, display the outputs to the screen while waiting for it to complete.
     *
     * @param command
     *            the full command with arguments to run
     * @return the exit value
     */
    public static int executeAndWait(String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(command);

            process.getOutputStream().close();
            StreamsTools.flowStreamNonBlocking(process.getInputStream(), System.out);
            StreamsTools.flowStreamNonBlocking(process.getErrorStream(), System.err);
            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Execute the command, display the outputs to the screen while waiting for it to complete.
     *
     * @param arguments
     *            the command and then its arguments
     * @return the exit value
     */
    public static int executeAndWait(String[] arguments) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(arguments);

            process.getOutputStream().close();
            StreamsTools.flowStreamNonBlocking(process.getInputStream(), System.out);
            StreamsTools.flowStreamNonBlocking(process.getErrorStream(), System.err);
            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    /**
     * Execute the command, display the outputs to the screen while waiting for it to complete.
     *
     * @param arguments
     *            the command and then its arguments
     * @param inputText
     *            some text to send to STDIN
     * @return the exit value
     */
    public static int executeAndWait(String[] arguments, String inputText) {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec(arguments);
            StreamsTools.flowStreamNonBlocking(process.getInputStream(), System.out);
            StreamsTools.flowStreamNonBlocking(process.getErrorStream(), System.err);

            // Send load
            process.getOutputStream().write(inputText.getBytes(CharsetTools.UTF_8));
            process.getOutputStream().close();

            process.waitFor();
            return process.exitValue();
        } catch (Exception e) {
            throw new SmallToolsException(e);
        }
    }

    private ConsoleTools() {
    }

}
