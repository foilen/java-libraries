/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.streampair.actions;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.hash.HashSha512;
import com.foilen.smalltools.streampair.StreamPair;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.StreamsTools;

/**
 * Share a common password between two machines. The validation can be done in plain text since it is never sending the password in clear text.
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
 * </pre>
 */
public class PasswordGateStreamPairAction extends AbstractTimeoutStreamPairAction {

    private static final Logger logger = LoggerFactory.getLogger(PasswordGateStreamPairAction.class);
    private static final int MAX_LENGTH = 1024 * 100;

    private int stringTokenLength = 10;
    private String password;

    public PasswordGateStreamPairAction() {
        negociationTimeoutSeconds = 20;
    }

    public String getPassword() {
        return password;
    }

    public int getStringTokenLength() {
        return stringTokenLength;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStringTokenLength(int stringTokenLength) {
        this.stringTokenLength = stringTokenLength;
    }

    @Override
    public StreamPair wrappedExecuteAction(StreamPair streamPair) {

        AssertTools.assertNotNull(password, "The password is not set for the gate");

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
        String hl = HashSha512.hashString(password) + tl + tr + password;
        String hr = HashSha512.hashString(password) + tr + tl + password;

        // Send our hash
        StreamsTools.write(output, hl);

        // Retrieve remote hash
        String receivedHash = StreamsTools.readString(input, MAX_LENGTH);

        // Validate remote hash
        if (!hr.equals(receivedHash)) {
            logger.info("The received hash {} is not the expected one {}. Dropping connection", receivedHash, hr);
            return null;
        }

        return streamPair;
    }

}