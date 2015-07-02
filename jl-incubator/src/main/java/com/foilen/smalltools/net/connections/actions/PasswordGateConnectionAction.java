/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.net.connections.actions;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.hash.HashSha512;
import com.foilen.smalltools.net.connections.Connection;
import com.foilen.smalltools.streampair.StreamPair;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * Share a common password between two machines. The validation can be done in plain text since it is never sending the password in clear text.
 * 
 * <pre>
 * Defaults:
 * - stringTokenLength = 10
 * - negociationTimeoutSeconds = 20
 * </pre>
 * 
 * <pre>
 * Here is the process:
 * - Send a random token (tl : token local)
 * - Get the other's token (tr : token remote)
 * - Verify tl != tr
 * - Send SHA-512(pw + tl + tr + pw)
 * - Get the received hash and compare it with SHA-512(pw + tr + tl + pw)
 * 
 * Security:
 * - Does not send the password in plain text (hash with salt)
 * - No replay-attack possible since the server has a say on the token it has to hash
 * - Cannot relay the remote token to another peer to get the answer since the other peer would send a different tr
 *   - NOT TRUE: The attacker could relay tr to another node and the tr of that other node to the first one
 * </pre>
 */
@Deprecated
public class PasswordGateConnectionAction extends AbstractTimeoutConnectionAction {

    private static final Logger logger = LoggerFactory.getLogger(PasswordGateConnectionAction.class);
    private static final int MAX_LENGTH = 1024 * 100;

    private int stringTokenLength = 10;
    private String password;

    public PasswordGateConnectionAction() {
        negociationTimeoutSeconds = 20;
    }

    public String getPassword() {
        return password;
    }

    public int getStringTokenLength() {
        return stringTokenLength;
    }

    public PasswordGateConnectionAction setPassword(String password) {
        this.password = password;
        return this;
    }

    public PasswordGateConnectionAction setStringTokenLength(int stringTokenLength) {
        this.stringTokenLength = stringTokenLength;
        return this;
    }

    @Override
    public Connection wrappedExecuteAction(Connection connection) {

        AssertTools.assertNotNull(password, "The password is not set for the gate");

        StreamPair streamPair = connection.getStreamPair();

        // Get the streams
        InputStream input = streamPair.getInputStream();
        OutputStream output = streamPair.getOutputStream();

        // Send a generated token
        String tl = SecureRandomTools.randomBase64String(stringTokenLength);
        StreamsTools.write(output, tl);

        // Retrieve the remote token
        String tr = StreamsTools.readString(input, MAX_LENGTH);

        // Compare tokens
        if (tl.equals(tr)) {
            logger.info("The received token is the same as mine. Dropping connection");
            return null;
        }

        // Compute the hashes
        String hl = HashSha512.hashString(password + tl + tr + password);
        String hr = HashSha512.hashString(password + tr + tl + password);

        // Send our hash
        StreamsTools.write(output, hl);

        // Retrieve remote hash
        String receivedHash = StreamsTools.readString(input, MAX_LENGTH);

        // Validate remote hash
        if (!hr.equals(receivedHash)) {
            logger.info("The received hash {} is not the expected one {}. Dropping connection", receivedHash, hr);
            return null;
        }

        return connection;
    }

}
