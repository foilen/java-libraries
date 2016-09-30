/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.reflection;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;

/**
 * Some helpers for reflection.
 * 
 * @deprecated Renamed to {@link ReflectionTools}.
 */
@Deprecated
public final class ReflectionUtils {

    /**
     * Find all the fields on the type and super-types.
     * 
     * @param clazz
     *            the class to get the fields
     * @return the fields
     */
    public static List<Field> allFields(Class<?> clazz) {
        AssertTools.assertNotNull(clazz, "The class cannot be null");

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
        AssertTools.assertNotNull(annotationClass, "The annotation cannot be null");
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
        AssertTools.assertNotNull(clazz, "The class cannot be null");

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
        AssertTools.assertNotNull(annotationClass, "The annotation cannot be null");
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
        AssertTools.assertNotNull(clazz, "The class cannot be null");
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
        AssertTools.assertNotNull(clazz, "The class cannot be null");

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
     * Copy all the bean's properties. Useful for Transfer Objects and Forms.
     * 
     * @param from
     *            the object to copy from
     * @param to
     *            the object to copy to
     */
    public static void copyAllProperties(Object from, Object to) {

        AssertTools.assertNotNull(from, "The from cannot be null");
        AssertTools.assertNotNull(to, "The to cannot be null");
        try {
            // Get all the properties on the source and destination
            BeanInfo fromBeanInfo = Introspector.getBeanInfo(from.getClass());
            BeanInfo toBeanInfo = Introspector.getBeanInfo(to.getClass());
            PropertyDescriptor[] fromPropertyDescriptors = fromBeanInfo.getPropertyDescriptors();
            PropertyDescriptor[] toPropertyDescriptors = toBeanInfo.getPropertyDescriptors();

            // Map the properties names for the destination
            Map<String, PropertyDescriptor> toPropertyDescriptorByName = new HashMap<>();
            for (PropertyDescriptor toPropertyDescriptor : toPropertyDescriptors) {
                if (toPropertyDescriptor.getWriteMethod() != null) {
                    toPropertyDescriptorByName.put(toPropertyDescriptor.getName(), toPropertyDescriptor);
                }
            }

            // Copy all fields when they exists on the destination
            for (PropertyDescriptor fromPropertyDescriptor : fromPropertyDescriptors) {
                PropertyDescriptor toPropertyDescriptor = toPropertyDescriptorByName.get(fromPropertyDescriptor.getName());

                // Validate exists
                if (toPropertyDescriptor == null) {
                    continue;
                }

                // Validate type or sub-type
                Class<?> fromType = fromPropertyDescriptor.getPropertyType();
                Class<?> toType = toPropertyDescriptor.getPropertyType();
                if (!toType.isAssignableFrom(fromType)) {
                    continue;
                }

                // Copy
                toPropertyDescriptor.getWriteMethod().invoke(to, fromPropertyDescriptor.getReadMethod().invoke(from));
            }

        } catch (Exception e) {
            throw new SmallToolsException("Problem copying properties", e);
        }
    }

    /**
     * Find the annotation set on the class and the field.
     * 
     * @param <T>
     *            the type of the annotation
     * @param clazz
     *            the class to get the field
     * @param fieldName
     *            the name of the field
     * @param annotationClass
     *            the desired annotation on the field
     * @return the annotation or null
     */
    public static <T extends Annotation> T findAnnotationByFieldNameAndAnnotation(Class<?> clazz, String fieldName, Class<T> annotationClass) {
        for (Class<?> oneClass : allTypes(clazz)) {
            Field field;
            try {
                field = oneClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException | SecurityException e) {
                continue;
            }

            T annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    /**
     * Create an instance of the specified class or throw an exception if there is an issue.
     * 
     * @param clazz
     *            the type
     * @param contructorParams
     *            the parameters of the constructor
     * @param <T>
     *            the type of the object
     * @return the object
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> clazz, Object... contructorParams) {
        // Find the contructor param types
        List<Class<?>> contructorParamTypes = new ArrayList<>(contructorParams.length);
        for (Object contructorParam : contructorParams) {
            if (contructorParam == null) {
                contructorParamTypes.add(null);
            } else {
                contructorParamTypes.add(contructorParam.getClass());
            }
        }

        // If only one constructor, use it
        Constructor<?>[] constructors = clazz.getConstructors();
        if (constructors.length == 1) {
            try {
                return (T) constructors[0].newInstance(contructorParams);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new SmallToolsException("Could not instanciate the class " + clazz.getName(), e);
            }
        }

        // Find the first constructor that support the specified types
        for (Constructor<?> constructor : constructors) {

            // Not the same amount of parameters
            Class<?>[] currentTypes = constructor.getParameterTypes();
            if (currentTypes.length != contructorParamTypes.size()) {
                continue;
            }

            // Not the right type
            boolean allRightTypes = true;
            for (int i = 0; i < currentTypes.length; ++i) {
                Class<?> contructorParamType = contructorParamTypes.get(i);
                if (contructorParamType == null) {
                    continue;
                }
                if (!currentTypes.equals(contructorParamType) && !currentTypes[i].isAssignableFrom(contructorParamType)) {
                    allRightTypes = false;
                    break;
                }
            }

            if (!allRightTypes) {
                continue;
            }

            // Good one
            try {
                return (T) constructor.newInstance(contructorParams);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new SmallToolsException("Could not instanciate the class " + clazz.getName(), e);
            }
        }

        // Fail since could not find a constructor
        throw new SmallToolsException("Could not instanciate the class " + clazz.getName() + " since couldn't find the right constructor");
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
        AssertTools.assertNotNull(object, "You must set an object");
        AssertTools.assertNotNull(visitField, "You must have a visitor");

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
        AssertTools.assertNotNull(objects, "You must set an objects array");
        AssertTools.assertNotNull(visitField, "You must have a visitor");

        for (Object object : objects) {
            visitAllFields(object, visitField);
        }
    }

    private ReflectionUtils() {
    }
}
