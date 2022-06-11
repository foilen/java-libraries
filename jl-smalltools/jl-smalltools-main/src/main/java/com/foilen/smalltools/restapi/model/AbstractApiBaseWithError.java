/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

/**
 * A single error field. Good when only needs a success or error.
 *
 * <pre>
 * Dependencies:
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 */
public abstract class AbstractApiBaseWithError extends AbstractApiBase {

    private ApiError error;

    public ApiError getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public AbstractApiBaseWithError setError(ApiError error) {
        this.error = error;
        return this;
    }

}
