/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2024 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.jsch;

import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FileTools;
import com.google.common.base.Strings;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A class to help login to a SSH server.
 */
public class SshLogin extends AbstractBasics {

    private String hostname;
    private String username;
    private int port = 22;

    private boolean ignoreHostCheck = false;
    private String knownHostsFile;
    private boolean autoApproveHostKey;

    private String password;
    private Set<String> privateKeyFile = new HashSet<>();

    private List<Consumer<JSch>> configureJschHooks = new ArrayList<>();
    private List<Consumer<Session>> configureSessionHooks = new ArrayList<>();

    /**
     * Where to log in.
     *
     * @param hostname the hostname
     * @param username the username
     */
    public SshLogin(String hostname, String username) {
        this.hostname = hostname;
        this.username = username;

        String possibleKnownHostsFile = JavaEnvironmentValues.getHomeDirectory() + File.separator + ".ssh/known_hosts";
        if (FileTools.exists(possibleKnownHostsFile)) {
            logger.info("Known hosts file {} exists. Will use it per default", possibleKnownHostsFile);
            knownHostsFile = possibleKnownHostsFile;
        } else {
            logger.info("Known hosts file {} does not exist. You must set it if you want to use it", possibleKnownHostsFile);
        }
    }

    /**
     * Add a hook for configuring JSch.
     *
     * @param hook the hook
     * @return this
     */
    public SshLogin addConfigureJschHook(Consumer<JSch> hook) {
        configureJschHooks.add(hook);
        return this;
    }

    /**
     * Add a hook for configuring the session.
     *
     * @param hook the hook
     * @return this
     */
    public SshLogin addConfigureSessionHook(Consumer<Session> hook) {
        configureSessionHooks.add(hook);
        return this;
    }

    /**
     * Tells to auto approve new host key. If the host key was previously approved, it is stored in the known hosts file and the key must match.
     *
     * @return this
     */
    public SshLogin autoApproveHostKey() {
        this.autoApproveHostKey = true;
        return this;
    }

    /**
     * Configure before getting a session.
     *
     * @param jSch the jsch instance
     */
    public void configure(JSch jSch) {

        if (!Strings.isNullOrEmpty(knownHostsFile)) {
            logger.info("Using known hosts file {}", knownHostsFile);
            try {
                jSch.setKnownHosts(knownHostsFile);
            } catch (JSchException e) {
                throw new SmallToolsException("Problem loading the known hosts file", e);
            }
        }

        privateKeyFile.forEach(keyfile -> {
            logger.info("Using keyfile {}", keyfile);
            try {
                jSch.addIdentity(keyfile);
            } catch (JSchException e) {
                logger.error("Could not use private key file " + keyfile, e);
            }
        });

        configureJschHooks.forEach(hook -> hook.accept(jSch));
    }

    /**
     * Configure before connecting the session.
     *
     * @param session the session instance
     */
    public void configure(Session session) {

        if (!Strings.isNullOrEmpty(password)) {
            logger.info("Using password");
            session.setPassword(password);
        }

        if (ignoreHostCheck) {
            logger.info("Ignoring host key check");
            session.setConfig("StrictHostKeyChecking", "no");
        }

        if (autoApproveHostKey) {
            session.setUserInfo(new AutoApproveUserinfo());
        }

        configureSessionHooks.forEach(hook -> hook.accept(session));
    }

    /**
     * Get the hostname to connect to.
     *
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Get the port to connect to.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the username to connect with.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Tells to connect to any host without verifying its key.
     *
     * @return this
     */
    public SshLogin ignoreHostCheck() {
        this.ignoreHostCheck = true;
        return this;
    }

    /**
     * Choose the hostname to connect to.
     *
     * @param hostname the hostname
     * @return this
     */
    public SshLogin setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    /**
     * Choose the username to connect with.
     *
     * @param username the username
     * @return this
     */
    public SshLogin setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Choose the file with the known hosts keys.
     *
     * @param knownHostsFile the file
     * @return this
     */
    public SshLogin useKnownHostsFile(String knownHostsFile) {
        this.knownHostsFile = knownHostsFile;
        return this;
    }

    /**
     * Choose the port to connect to.
     *
     * @param port the port
     * @return this
     */
    public SshLogin usePort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set a password to use if no private key are working.
     *
     * @param password the password
     * @return this
     */
    public SshLogin withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Add a keyfile to the list of available ones.
     *
     * @param keyfile the private key file
     * @return this
     */
    public SshLogin withPrivateKey(String keyfile) {
        this.privateKeyFile.add(keyfile);
        return this;
    }

}
