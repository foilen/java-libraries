/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.foilen.smalltools.Assert;
import com.foilen.smalltools.exception.SmallToolsException;

/**
 * Some helpers for reflection.
 */
public final class ReflectionUtils {
    /**
     * Find all the fields on the type and super-types.
     * 
     * @param clazz
     *            the class to get the fields
     * @return the fields
     */
    public static List<Field> allFields(Class<?> clazz) {
        Assert.assertNotNull(clazz, "The class cannot be null");

        List<Field> result = new ArrayList<>();
        for (Class<?> currentType : allTypes(clazz)) {
            for (Field field : currentType.getDeclaredFields()) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * Find all the fields on the type and super-types that has the specified annotation.
     * 
     * @param clazz
     *            the class to get the fields
     * @param annotationClass
     *            the desired annotation on the field
     * @return the fields with the specified annotation
     */
    public static List<Field> allFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Assert.assertNotNull(annotationClass, "The annotation cannot be null");
        List<Field> result = new ArrayList<>();

        for (Field field : allFields(clazz)) {
            if (field.getAnnotation(annotationClass) != null) {
                result.add(field);
            }
        }

        return result;
    }

    /**
     * Find all the methods on the type and super-types.
     * 
     * @param clazz
     *            the class to get the fields
     * @return the methods
     */
    public static List<Method> allMethods(Class<?> clazz) {
        Assert.assertNotNull(clazz, "The class cannot be null");

        List<Method> result = new ArrayList<>();
        for (Class<?> currentType : allTypes(clazz)) {
            for (Method method : currentType.getDeclaredMethods()) {
                result.add(method);
            }
        }

        return result;
    }

    /**
     * Find all the methods on the type and super-types that has the specified annotation.
     * 
     * @param clazz
     *            the class to get the fields
     * @param annotationClass
     *            the desired annotation on the field
     * @return the methods with the specified annotation
     */
    public static List<Method> allMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Assert.assertNotNull(annotationClass, "The annotation cannot be null");
        List<Method> result = new ArrayList<>();

        for (Method method : allMethods(clazz)) {
            if (method.getAnnotation(annotationClass) != null) {
                result.add(method);
            }
        }

        return result;
    }

    /**
     * Find all the the type and super-types.
     * 
     * @param clazz
     *            the class
     * @return all the types and super-types (including the specified one)
     */
    public static List<Class<?>> allTypes(Class<?> clazz) {
        Assert.assertNotNull(clazz, "The class cannot be null");
        List<Class<?>> result = new ArrayList<>();
        allTypes(result, clazz);
        return result;
    }

    /**
     * Find all the the type and super-types.
     * 
     * @param classes
     *            the list that will be filled with the current class and the super ones
     * @param clazz
     *            the class
     */
    private static void allTypes(List<Class<?>> classes, Class<?> clazz) {
        Assert.assertNotNull(clazz, "The class cannot be null");

        // Current one
        classes.add(clazz);

        // Interfaces
        for (Class<?> superClass : clazz.getInterfaces()) {
            allTypes(classes, superClass);
        }

        // Extended class
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            allTypes(classes, superClass);
        }
    }

    /**
     * Create an instance of the specified class or throw an exception if there is an issue.
     * 
     * @param clazz
     *            the type
     * @return the object
     */
    public static <T> T instantiate(Class<T> clazz) {
        try {
            T object = clazz.newInstance();
            return object;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SmallToolsException("Could not instanciate the class " + clazz.getName(), e);
        }
    }

    /**
     * Get the class of the specified type if it exists or null.
     * 
     * @param className
     *            the full package + class name
     * @return the class or null
     */
    public static Class<?> safelyGetClass(String className) {
        Class<?> clazz = null;

        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Ignore since we want null
        }

        return clazz;
    }

    /**
     * Visit all the fields of an object.
     * 
     * @param object
     *            the object to visit
     * @param visitField
     *            the method to execute on each field
     */
    public static void visitAllFields(Object object, VisitField visitField) {
        Assert.assertNotNull(object, "You must set an object");
        Assert.assertNotNull(visitField, "You must have a visitor");

        for (Field field : ReflectionUtils.allFields(object.getClass())) {
            visitField.visitField(field, object);
        }
    }

    /**
     * Visit all the fields of all the objects.
     * 
     * @param objects
     *            the objects to visit
     * @param visitField
     *            the method to execute on each field
     */
    public static void visitAllFields(Object[] objects, VisitField visitField) {
        Assert.assertNotNull(objects, "You must set an objects array");
        Assert.assertNotNull(visitField, "You must have a visitor");

        for (Object object : objects) {
            visitAllFields(object, visitField);
        }
    }

    private ReflectionUtils() {
    }
}
