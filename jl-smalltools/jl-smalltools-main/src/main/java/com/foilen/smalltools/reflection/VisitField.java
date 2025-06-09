package com.foilen.smalltools.reflection;

import java.lang.reflect.Field;

/**
 * Visitor pattern for all the fields of an object.
 */
public interface VisitField {

    /**
     * Visit the field
     *
     * @param field  the field
     * @param object the object
     */
    void visitField(Field field, Object object);

}
