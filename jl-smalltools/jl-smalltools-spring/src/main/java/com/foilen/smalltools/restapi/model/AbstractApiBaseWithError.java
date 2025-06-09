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
