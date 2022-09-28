/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.restapi.services;

import com.foilen.smalltools.restapi.model.FormResult;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.DateTools;
import com.foilen.smalltools.tools.StringTools;
import com.google.common.base.Strings;

import java.util.regex.Pattern;

/**
 * Some methods to validate a form and store errors in the {@link FormResult}.
 */
public class FormValidationTools {

    private static final Pattern alphaNum = Pattern.compile("[A-Za-z0-9]*");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    public static void validateAlphaNumExtra(FormResult formResult, String fieldName, String fieldValue) {

        if (Strings.isNullOrEmpty(fieldValue)) {
            return;
        }

        if (!alphaNum.matcher(fieldValue).matches()) {
            CollectionsTools.getOrCreateEmptyArrayList(formResult.getValidationErrorsByField(), fieldName, String.class).add("Format is alphanumeric");
        }

    }

    public static void validateAtLeastOneManadatory(FormResult formResult, String[] fieldNames, String[] fieldValues) {

        for (String fieldValue : fieldValues) {
            if (!Strings.isNullOrEmpty(fieldValue)) {
                return;
            }
        }

        for (String fieldName : fieldNames) {
            CollectionsTools.getOrCreateEmptyArrayList(formResult.getValidationErrorsByField(), fieldName, String.class).add("At least one value must be entered");
        }

    }

    public static void validateDateOnly(FormResult formResult, String fieldName, String date) {

        if (Strings.isNullOrEmpty(date)) {
            return;
        }

        boolean goodFormat = false;
        try {
            String expectedDate = DateTools.formatDateOnly(DateTools.parseDateOnly(date));
            goodFormat = StringTools.safeEquals(expectedDate, date);
        } catch (Exception e) {
        }

        if (!goodFormat) {
            CollectionsTools.getOrCreateEmptyArrayList(formResult.getValidationErrorsByField(), fieldName, String.class).add("Format is yyyy-MM-dd");
        }

    }

    public static void validateEmail(FormResult formResult, String fieldName, String fieldValue) {

        if (Strings.isNullOrEmpty(fieldValue)) {
            return;
        }

        if (!EMAIL_PATTERN.matcher(fieldValue).matches()) {
            CollectionsTools.getOrCreateEmptyArrayList(formResult.getValidationErrorsByField(), fieldName, String.class).add("Is not an email");
        }
    }

    public static void validateMandatory(FormResult formResult, String fieldName, String fieldValue) {

        if (Strings.isNullOrEmpty(fieldValue)) {
            CollectionsTools.getOrCreateEmptyArrayList(formResult.getValidationErrorsByField(), fieldName, String.class).add("The value is mandatory");
        }

    }

}
