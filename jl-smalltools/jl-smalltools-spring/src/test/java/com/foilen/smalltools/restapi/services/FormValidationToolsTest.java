/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2025 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.services;

import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.test.asserts.AssertTools;
import org.junit.Test;

import java.util.Collections;

public class FormValidationToolsTest {

    private static final String MY_FIELD = "myField";

    private void validateSuccess(String email) {
        FormResult formResult = new FormResult();
        FormValidationTools.validateEmail(formResult, MY_FIELD, email);
        FormResult expected = new FormResult();
        AssertTools.assertJsonComparison(expected, formResult);
    }

    private void validateError(String email) {
        FormResult formResult = new FormResult();
        FormValidationTools.validateEmail(formResult, MY_FIELD, email);
        FormResult expected = new FormResult();
        expected.getValidationErrorsByField().put(MY_FIELD, Collections.singletonList("Is not an email"));
        AssertTools.assertJsonComparison(expected, formResult);
    }

    @Test
    public void validateEmail_OK() {
        validateSuccess("admin@foilen.com");
    }

    @Test
    public void validateEmail_NoAt() {
        validateError("adminfoilen.com");
    }

    @Test
    public void validateEmail_NoDomain() {
        validateError("admin@");
    }

    @Test
    public void validateEmail_NoUser() {
        validateError("@foilen.com");
    }

}