/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.jsch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.function.ConsumerWithException;
import com.foilen.smalltools.shell.ExecResult;
import com.foilen.smalltools.shell.ExecResultInFiles;
import com.foilen.smalltools.shell.ExecResultInMemory;
import com.foilen.smalltools.shell.ExecResultOnlyExitCode;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.StreamsTools;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * A tool to help executing SSH commands and use SFTP.
 *
 * Some usage:
 *
 * <pre>
 *
 * // Check if you can connect to the remote host
 * SshLogin sshLogin = new SshLogin("m1.example.com", "user").withPassword("myGoodPassword");
 * if (JSchTools.canLogin(sshLogin)) {
 *     System.out.println("Yes you can");
 * } else {
 *     System.out.println("No you can't");
 * }
 *
 * // Log in using password
 * JSchTools l1 = new JSchTools().login(sshLogin);
 *
 * // Execute a command and keep the stdout and stderr in memory (if the output is very long, use executeInFile instead which will stream the outputs to a temporary file and read back from it)
 * ExecResult execResult = l1.executeInMemory("/bin/ps -aux");
 *
 * // Display as strings
 * System.out.println("Exit: " + execResult.getExitCode());
 * System.out.println("Out: " + execResult.getStdOutAsString());
 * System.out.println("Err: " + execResult.getStdErrAsString());
 *
 * // Process the output per line
 * execResult.getStdOutAsLines().forEach(line -> {
 *     System.out.println("Line: " + line);
 * });
 *
 * // Create a temporary file
 * File file = File.createTempFile("file", ".txt");
 * FileTools.writeFile("hello", file);
 *
 * // Use SFTP
 * l1.createAndUseSftpChannel(sftp -> {
 *
 *     System.out.println("Sending " + file.getAbsolutePath());
 *     sftp.put(file.getAbsolutePath(), "hello.txt");
 *
 *     System.out.println("Listing: ");
 *     Vector<LsEntry> files = sftp.ls(".");
 *     files.forEach(it -> {
 *         System.out.println("\t" + JsonTools.compactPrint(it));
 *     });
 *
 *     System.out.println("Changing mod:");
 *     sftp.chmod(00644, "hello.txt");
 *
 *     files = sftp.ls("hello.txt");
 *     files.forEach(it -> {
 *         System.out.println("\t" + JsonTools.compactPrint(it));
 *     });
 *
 *     System.out.println("Deleting");
 *     sftp.rm("hello.txt");
 *
 * });
 *
 * // Disconnect
 * l1.disconnect();
 * </pre>
 *
 * <pre>
 * Dependencies:
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * compile 'com.jcraft:jsch:0.1.54'
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 */
public class JSchTools extends AbstractBasics {

    private final static Logger logger = LoggerFactory.getLogger(JSchTools.class);

    private static final Set<PosixFilePermission> TMP_FILES_PERMS = new HashSet<>(Arrays.asList(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));

    /**
     * Check if it is possible to log on the remote machine.
     *
     * @param sshLogin
     *            the parameters when logging in
     * @return true if can log in
     */
    public static boolean canLogin(SshLogin sshLogin) {
        JSch jSch = new JSch();
        Session session = null;
        ChannelExec channel = null;
        try {
            logger.info("Trying to log on {} with user {}", sshLogin.getHostname(), sshLogin.getUsername());
            sshLogin.configure(jSch);
            session = jSch.getSession(sshLogin.getUsername(), sshLogin.getHostname(), sshLogin.getPort());
            sshLogin.configure(session);
            session.connect();
            return true;
        } catch (Exception e) {
            logger.error("Could not connect", e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return false;
    }

    private JSch jSch;
    private Session session;

    /**
     * Get hold of any Channel. When done, it will be disconnected automatically.
     *
     * @param channelType
     *            the type of the channel. You can view the list in {@link Channel}
     * @param channelClass
     *            the class of the channel
     * @param configureChannel
     *            any configuration needed before connecting the channel
     * @param consumer
     *            the SFTP Channel consumer on which you can do operations
     */
    @SuppressWarnings("unchecked")
    public <C extends Channel> void createAndUseChannel(String channelType, Class<C> channelClass, Consumer<C> configureChannel, ConsumerWithException<C, Exception> consumer) {

        if (session == null || !session.isConnected()) {
            throw new SmallToolsException("Cannot open " + channelType + " channel because it is not connected");
        }

        C channel = null;
        try {
            channel = (C) session.openChannel(channelType);
            configureChannel.accept(channel);
            channel.connect();
            consumer.accept(channel);
        } catch (Exception e) {
            throw new SmallToolsException("Problem while executing " + channelType, e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
        }
    }

    /**
     * Get hold of an SFTP Channel. When done, it will be disconnected automatically.
     *
     * @param consumer
     *            the SFTP Channel consumer on which you can do operations
     */
    public void createAndUseSftpChannel(ConsumerWithException<ChannelSftp, Exception> consumer) {
        createAndUseChannel("sftp", ChannelSftp.class, channel -> {
        }, consumer);
    }

    /**
     * Disconnect if connected.
     */
    public void disconnect() {
        if (session != null && session.isConnected()) {
            logger.info("Disconnecting");
            session.disconnect();
        } else {
            logger.info("Already not connected");
        }
    }

    private int execute(String command, OutputStream out, OutputStream err) {

        AtomicInteger exitCode = new AtomicInteger(-1);

        createAndUseChannel("exec", ChannelExec.class, channel -> {
            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(err);
        }, channel -> {
            StreamsTools.flowStream(channel.getInputStream(), out);

            out.close();
            err.close();

            exitCode.set(channel.getExitStatus());
        });

        return exitCode.get();
    }

    /**
     * Execute a command and store the stdout and stderr in files. This is good if the output is expected to be big. Then, better stream per line or use the input stream (not retrieving the full
     * String else it will be loaded in memory)
     *
     * @param command
     *            the command and arguments
     * @return the execution's result
     */
    public ExecResult executeInFile(String command) {

        try {
            File out = File.createTempFile("out", ".txt");
            File err = File.createTempFile("err", ".txt");

            FileTools.changePermissions(out, false, TMP_FILES_PERMS);
            FileTools.changePermissions(err, false, TMP_FILES_PERMS);

            logger.debug("Storing stdout in {}", out.getAbsolutePath());
            logger.debug("Storing stderr in {}", err.getAbsolutePath());

            int exitCode = execute(command, new FileOutputStream(out), new FileOutputStream(err));

            return new ExecResultInFiles(out, err, exitCode);
        } catch (SmallToolsException e) {
            throw e;
        } catch (Exception e) {
            throw new SmallToolsException("Problem using temporary files", e);
        }
    }

    /**
     * Execute a command and display the stdout and stderr in the logs.
     *
     * @param command
     *            the command and arguments
     * @return the execution's result, but only the exit code is retrievable
     */
    public ExecResult executeInLogger(String command) {

        OutputStream out = StreamsTools.createLoggerOutputStream(logger, Level.INFO);
        OutputStream err = StreamsTools.createLoggerOutputStream(logger, Level.ERROR);

        int exitCode = execute(command, out, err);

        return new ExecResultOnlyExitCode(exitCode);
    }

    /**
     * Execute a command and store the stdout and stderr in memory. This is good if the output is expected to be small.
     *
     * @param command
     *            the command and arguments
     * @return the execution's result
     */
    public ExecResult executeInMemory(String command) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();

        int exitCode = execute(command, out, err);

        return new ExecResultInMemory(out, err, exitCode);
    }

    /**
     * Disconnect any previous connection and log on the remote machine.
     *
     * @param sshLogin
     *            the parameters when logging in
     * @return this
     */
    public JSchTools login(SshLogin sshLogin) {
        if (session != null && session.isConnected()) {
            logger.info("Disconnecting previous connection");
            session.disconnect();
        }
        try {
            logger.info("Log on {} with user {}", sshLogin.getHostname(), sshLogin.getUsername());
            jSch = new JSch();
            sshLogin.configure(jSch);
            session = jSch.getSession(sshLogin.getUsername(), sshLogin.getHostname(), sshLogin.getPort());
            sshLogin.configure(session);
            session.connect();
        } catch (Exception e) {
            session = null;
            throw new SmallToolsException("Could not connect", e);
        }

        return this;
    }

}
