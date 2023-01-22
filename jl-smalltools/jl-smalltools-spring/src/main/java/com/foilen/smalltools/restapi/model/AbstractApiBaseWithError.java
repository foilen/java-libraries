/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

/**
 * A single error field. Good when only needs a success or error.
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
