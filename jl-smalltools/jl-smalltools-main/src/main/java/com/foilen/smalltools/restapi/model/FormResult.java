/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * To show the results of a form submission including global errors and warnings and errors per field.
 *
 * <pre>
 * Dependencies:
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * </pre>
 *
 */
public class FormResult extends AbstractApiBaseWithError {

    private Map<String, List<String>> validationErrorsByField = new HashMap<>();
    private List<String> globalErrors = new ArrayList<String>();
    private List<String> globalWarnings = new ArrayList<String>();

    public List<String> getGlobalErrors() {
        return globalErrors;
    }

    public List<String> getGlobalWarnings() {
        return globalWarnings;
    }

    public Map<String, List<String>> getValidationErrorsByField() {
        return validationErrorsByField;
    }

    @Override
    public boolean isSuccess() {
        return validationErrorsByField.isEmpty() && globalErrors.isEmpty() && super.isSuccess();
    }

    public FormResult setGlobalErrors(List<String> globalErrors) {
        this.globalErrors = globalErrors;
        return this;
    }

    public FormResult setGlobalWarnings(List<String> globalWarnings) {
        this.globalWarnings = globalWarnings;
        return this;
    }

    public FormResult setValidationErrorsByField(Map<String, List<String>> validationErrorsByField) {
        this.validationErrorsByField = validationErrorsByField;
        return this;
    }

}
