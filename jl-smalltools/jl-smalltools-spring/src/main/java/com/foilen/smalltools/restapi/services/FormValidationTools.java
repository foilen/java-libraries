/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

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

    /**
     * Validate that the value is alphanumeric.
     *
     * @param formResult the result to add errors to
     * @param fieldName  the name of the field
     * @param fieldValue the value of the field
     */
    public static void validateAlphaNum(FormResult formResult, String fieldName, String fieldValue) {

        if (Strings.isNullOrEmpty(fieldValue)) {
            return;
        }

        if (!alphaNum.matcher(fieldValue).matches()) {
            CollectionsTools.getOrCreateEmptyArrayList(formResult.getValidationErrorsByField(), fieldName, String.class).add("Format is alphanumeric");
        }

    }

    /**
     * Validate that at least one of the values is not null or empty.
     *
     * @param formResult  the result to add errors to
     * @param fieldNames  the names of the fields
     * @param fieldValues the values of the fields
     */
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

    /**
     * Validate that the value is a date.
     *
     * @param formResult the result to add errors to
     * @param fieldName  the name of the field
     * @param date       the value of the field
     */
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

    /**
     * Validate that the value is an email.
     *
     * @param formResult the result to add errors to
     * @param fieldName  the name of the field
     * @param fieldValue the value of the field
     */
    public static void validateEmail(FormResult formResult, String fieldName, String fieldValue) {

        if (Strings.isNullOrEmpty(fieldValue)) {
            return;
        }

        if (!EMAIL_PATTERN.matcher(fieldValue).matches()) {
            CollectionsTools.getOrCreateEmptyArrayList(formResult.getValidationErrorsByField(), fieldName, String.class).add("Is not an email");
        }
    }

    /**
     * Validate that the value is present.
     *
     * @param formResult the result to add errors to
     * @param fieldName  the name of the field
     * @param fieldValue the value of the field
     */
    public static void validateMandatory(FormResult formResult, String fieldName, String fieldValue) {

        if (Strings.isNullOrEmpty(fieldValue)) {
            CollectionsTools.getOrCreateEmptyArrayList(formResult.getValidationErrorsByField(), fieldName, String.class).add("The value is mandatory");
        }

    }

}
