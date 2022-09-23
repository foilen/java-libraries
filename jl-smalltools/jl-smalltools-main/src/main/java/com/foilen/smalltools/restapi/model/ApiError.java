/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

import java.util.Date;

import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.SecureRandomTools;

/**
 * An error with some details.
 *
 * <pre>
 * Dependencies:
 * implementation 'org.apache.commons:commons-lang3:3.12.0'
 * implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.4'
 * implementation 'org.slf4j:slf4j-api:2.0.2'
 * </pre>
 */
public class ApiError extends AbstractApiBase {

    private String timestamp;
    private String uniqueId;
    private String message;

    public ApiError() {
    }

    public ApiError(Date timestamp, String uniqueId, String message) {
        this.timestamp = DateTools.formatFull(timestamp);
        this.uniqueId = uniqueId;
        this.message = message;
    }

    /**
     * Create an error message with a random 10 hex-based id and the current time.
     *
     * @param message
     *            the error message
     */
    public ApiError(String message) {
        this.timestamp = DateTools.formatFull(new Date());
        this.uniqueId = SecureRandomTools.randomHexString(10);
        this.message = message;
    }

    public ApiError(String timestamp, String uniqueId, String message) {
        this.timestamp = timestamp;
        this.uniqueId = uniqueId;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public ApiError setMessage(String message) {
        this.message = message;
        return this;
    }

    public ApiError setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ApiError setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }

}
