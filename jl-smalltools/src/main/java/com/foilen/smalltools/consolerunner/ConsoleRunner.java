/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.consolerunner;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.foilen.smalltools.FileLinesIterable;
import com.foilen.smalltools.TimeoutHandler;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tuple.Tuple2;

/**
 * An helper to run any applications.
 * 
 * <pre>
 *  Usage to have the out/err stream piped to the standard out/err.
 *  
 *     ConsoleRunner runner = new ConsoleRunner();
 *         runner.command = "apt-get";
 *         runner.addArguments("-y", "install");
 *         runner.addArguments(packageNames);
 * 
 *         int status = runner.execute();
 * </pre>
 *
 * <pre>
 *  Usage to get the output in a string.
 *  
 *     ConsoleRunner runner = new ConsoleRunner();
 *         runner.command = "dpkg";
 *         runner.addArguments("-s", packageName);
 * 
 *         String content = runner.executeForString();
 * </pre>
 * 
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:18.0'
 * compile 'org.slf4j:slf4j-api:1.7.21'
 * </pre>
 */
public class ConsoleRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleRunner.class);

    private Long timeoutInMilliseconds = null;

    private String command;

    private String workingDirectory;
    private List<String> arguments = new ArrayList<>();
    private Map<String, String> environments = new HashMap<>();
    private boolean overrideEnvironment = false;
    private InputStream consoleInput;
    private OutputStream consoleOutput = System.out;
    private OutputStream consoleError = System.err;
    private boolean redirectErrorStream = false;
    private boolean closeConsoleOutput = false;
    private boolean closeConsoleError = false;

    private int statusCode;

    /**
     * Add arguments to the command.
     * 
     * @param arguments
     *            the arguments
     * @return this
     */
    public ConsoleRunner addArguments(List<String> arguments) {
        this.arguments.addAll(arguments);
        return this;
    }

    /**
     * Add arguments to the command.
     * 
     * @param arguments
     *            the arguments
     * @return this
     */
    public ConsoleRunner addArguments(String... arguments) {
        for (String argument : arguments) {
            this.arguments.add(argument);
        }
        return this;
    }

    /**
     * Add environment to the command.
     * 
     * @param name
     *            the environment name
     * @param value
     *            the environment value
     * @return this
     */
    public ConsoleRunner addEnvironment(String name, String value) {
        this.environments.put(name, value);
        return this;
    }

    /**
     * Execute the command using all the configured console input/output/error.
     * 
     * @return the status code
     */
    public int execute() {

        ConsoleTimeoutHandlerRunnable consoleTimeoutHandlerRunnable = new ConsoleTimeoutHandlerRunnable(this);

        if (timeoutInMilliseconds == null) {
            // No timeout
            consoleTimeoutHandlerRunnable.run();
            statusCode = consoleTimeoutHandlerRunnable.result();
        } else {
            // With timeout
            TimeoutHandler<Integer> handler = new TimeoutHandler<Integer>(timeoutInMilliseconds, consoleTimeoutHandlerRunnable);
            try {
                statusCode = handler.call();
            } catch (InterruptedException e) {
                logger.debug("The console timed out");
                throw new ConsoleTimedoutException();
            }
        }

        return statusCode;
    }

    /**
     * Execute the command using all the configured console input/error and returns the console output as an iterator of line String (can be used in a foreach loop as well).
     * 
     * @return the console output as a line iterator
     */
    public FileLinesIterable executeForLineIterator() {
        // Execute
        String output = executeForString();

        // Return iterator
        FileLinesIterable fileLinesIterable = new FileLinesIterable();
        fileLinesIterable.openString(output);
        return fileLinesIterable;
    }

    /**
     * Execute the command using all the configured console input/error and returns the console output as a String.
     * 
     * @return the console output
     */
    public String executeForString() {
        // Configure the console output
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        consoleOutput = byteArrayOutputStream;

        // Execute
        execute();

        // Return String
        return byteArrayOutputStream.toString();
    }

    /**
     * Execute the command using all the configured console input and returns the console output and console error as separate Strings.
     * 
     * @return the console output and error
     */
    public Tuple2<String, String> executeForStrings() {
        // Configure the console output and error
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        consoleOutput = byteArrayOutputStream;

        ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream();
        consoleError = byteArrayErrorStream;

        redirectErrorStream = false;

        // Execute
        execute();

        // Return String
        return new Tuple2<>(byteArrayOutputStream.toString(), byteArrayErrorStream.toString());
    }

    /**
     * Execute the command using all the configured console input/error and display the output stream using the logger line by line.
     * 
     * @param outputLogger
     *            the logger to use
     * @param level
     *            the level to use on the logger
     */
    public void executeWithLogger(Logger outputLogger, Level level) {
        // Configure the output
        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream;
        try {
            pipedOutputStream = new PipedOutputStream(pipedInputStream);
        } catch (IOException e) {
            throw new SmallToolsException("Problem setting the pipe", e);
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(pipedInputStream));
        consoleOutput = pipedOutputStream;
        closeConsoleOutput = true;

        new Thread(() -> {
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    switch (level) {
                    case DEBUG:
                        outputLogger.debug("{}", line);
                        break;
                    case ERROR:
                        outputLogger.error("{}", line);
                        break;
                    case INFO:
                        outputLogger.info("{}", line);
                        break;
                    case TRACE:
                        outputLogger.trace("{}", line);
                        break;
                    case WARN:
                        outputLogger.warn("{}", line);
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Error while reading the output stream", e);
            }

        }, "STD OUT").start();

        // Execute
        execute();
    }

    public List<String> getArguments() {
        return arguments;
    }

    public String getCommand() {
        return command;
    }

    public OutputStream getConsoleError() {
        return consoleError;
    }

    public InputStream getConsoleInput() {
        return consoleInput;
    }

    public OutputStream getConsoleOutput() {
        return consoleOutput;
    }

    public Map<String, String> getEnvironments() {
        return environments;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Long getTimeoutInMilliseconds() {
        return timeoutInMilliseconds;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public boolean isCloseConsoleError() {
        return closeConsoleError;
    }

    public boolean isCloseConsoleOutput() {
        return closeConsoleOutput;
    }

    public boolean isOverrideEnvironment() {
        return overrideEnvironment;
    }

    public boolean isRedirectErrorStream() {
        return redirectErrorStream;
    }

    /**
     * Set all the arguments (overriding any previously set).
     * 
     * @param arguments
     *            the list
     * @return this
     */
    public ConsoleRunner setArguments(List<String> arguments) {
        this.arguments = arguments;
        return this;
    }

    /**
     * True to close the console error stream set by {@link #setConsoleError(OutputStream)} when the process ends.
     * 
     * @param closeConsoleError
     *            true to close
     * @return this
     */
    public ConsoleRunner setCloseConsoleError(boolean closeConsoleError) {
        this.closeConsoleError = closeConsoleError;
        return this;
    }

    /**
     * True to close the console output stream set by {@link #setConsoleOutput(OutputStream)} when the process ends.
     * 
     * @param closeConsoleOutput
     *            true to close
     * @return this
     */
    public ConsoleRunner setCloseConsoleOutput(boolean closeConsoleOutput) {
        this.closeConsoleOutput = closeConsoleOutput;
        return this;
    }

    /**
     * Set the command to run.
     * 
     * @param command
     *            the command
     * @return this
     */
    public ConsoleRunner setCommand(String command) {
        this.command = command;
        return this;
    }

    /**
     * Set where to send the STD error stream. Default {@link System#err}
     * 
     * @param consoleError
     *            the stream
     * @return this
     */
    public ConsoleRunner setConsoleError(OutputStream consoleError) {
        this.consoleError = consoleError;
        return this;
    }

    /**
     * Set where to get the STD in from. Default none
     * 
     * @param consoleInput
     *            the stream
     * @return this
     */
    public ConsoleRunner setConsoleInput(InputStream consoleInput) {
        this.consoleInput = consoleInput;
        return this;
    }

    /**
     * Set where to send the STD out stream. Default {@link System#out}
     * 
     * @param consoleOutput
     *            the stream
     * @return this
     */
    public ConsoleRunner setConsoleOutput(OutputStream consoleOutput) {
        this.consoleOutput = consoleOutput;
        return this;
    }

    /**
     * Set all the environments (overriding any previously set).
     * 
     * @param environments
     *            the new environments
     * @return this
     */
    public ConsoleRunner setEnvironments(Map<String, String> environments) {
        this.environments = environments;
        return this;
    }

    /**
     * Tells if the environments should be cleared before adding the one configured with {@link #addEnvironment(String, String)}.
     * 
     * @param overrideEnvironment
     *            true to override
     * @return this
     */
    public ConsoleRunner setOverrideEnvironment(boolean overrideEnvironment) {
        this.overrideEnvironment = overrideEnvironment;
        return this;
    }

    /**
     * Merge the STD error stream to STD output stream.
     * 
     * @param redirectErrorStream
     *            true to redirect
     * @return this
     */
    public ConsoleRunner setRedirectErrorStream(boolean redirectErrorStream) {
        this.redirectErrorStream = redirectErrorStream;
        return this;
    }

    /**
     * Set a timeout. Default null (none)
     * 
     * @param timeoutInMilliseconds
     *            how many milliseconds or null for none
     * @return this
     */
    public ConsoleRunner setTimeoutInMilliseconds(Long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
        return this;
    }

    /**
     * Set the working directory.
     * 
     * @param workingDirectory
     *            the working directory
     * @return this
     */
    public ConsoleRunner setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

}
