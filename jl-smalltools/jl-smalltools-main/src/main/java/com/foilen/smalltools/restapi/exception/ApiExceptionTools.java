/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.exception;

import com.foilen.smalltools.restapi.model.AbstractApiBaseWithError;
import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.CollectionsTools;

public class ApiExceptionTools {

    public static void throwIfFailure(String context, AbstractApiBaseWithError result) {
        if (result == null) {
            throw new ApiException(context + " : The response is null");
        }
        if (!result.isSuccess()) {
            throw new ApiException(context + " : " + result.getError());
        }
    }

    public static void throwIfFailure(String context, FormResult result) {
        if (result == null) {
            throw new ApiException(context + " : The response is null");
        }
        if (!result.isSuccess()) {
            StringBuilder message = new StringBuilder();
            if (result.getError() != null) {
                message.append(" error: ").append(result.getError());
            }
            if (!CollectionsTools.isNullOrEmpty(result.getGlobalErrors())) {
                message.append(" global errors: ").append(result.getGlobalErrors());
            }
            if (result.getValidationErrorsByField() != null && !result.getValidationErrorsByField().isEmpty()) {
                message.append(" validation errors: ").append(result.getValidationErrorsByField());
            }
            throw new ApiException(context + " :" + message.toString());
        }
    }

}
