/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.reflection;

import java.lang.reflect.Field;

/**
 * Visitor pattern for all the fields of an object.
 */
public interface VisitField {

    /**
     * Visit the field
     *
     * @param field
     *            the field
     * @param object
     *            the object
     */
    void visitField(Field field, Object object);

}
