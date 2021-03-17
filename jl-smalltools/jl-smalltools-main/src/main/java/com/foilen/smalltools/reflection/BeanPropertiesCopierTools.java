/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.reflection;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.TypeMismatchException;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.AssertTools;

/**
 * Helps copying properties from one bean to another bean. It can also update a collection's items on place. See {@link #updateCollection(String)}.
 *
 * <pre>
 * Employee employee = ...;
 * EmployeeTo employeeTo = new EmployeeTo();
 *
 * BeanPropertiesCopierTools copierTools = new BeanPropertiesCopierTools(employee, employeeTo);
 *
 * // Same property name
 * copierTools.copyProperty("firstName");
 * copierTools.copyProperty("LastName");
 *
 * //
 * copierTools.copyProperty("LastName");
 *
 * // Different property name
 * copierTools.copyProperty("address", "homeAddress");
 * </pre>
 *
 * <pre>
 * Dependencies:
 * compile 'org.apache.commons:commons-lang3:3.6'
 * compile 'org.slf4j:slf4j-api:1.7.25'
 * compile 'org.springframework:spring-orm:4.3.11.RELEASE'
 * </pre>
 */
public class BeanPropertiesCopierTools {

    private BeanWrapper sourceWrapper;
    private BeanWrapper destinationWrapper;

    /**
     * Provide the bean wrappers.
     *
     * @param sourceWrapper
     *            the source
     * @param destinationWrapper
     *            the destination
     */
    public BeanPropertiesCopierTools(BeanWrapper sourceWrapper, BeanWrapper destinationWrapper) {
        AssertTools.assertNotNull(sourceWrapper, "The sourceWrapper cannot be null");
        AssertTools.assertNotNull(sourceWrapper.getWrappedInstance(), "The source cannot be null");
        AssertTools.assertNotNull(destinationWrapper, "The destinationWrapper cannot be null");
        AssertTools.assertNotNull(destinationWrapper.getWrappedInstance(), "The destination cannot be null");

        this.sourceWrapper = sourceWrapper;
        this.destinationWrapper = destinationWrapper;
    }

    /**
     * Provide the source object and the destination class to instantiate. You can then retrieve the destination with {@link #getDestination()}.
     *
     * @param source
     *            the source
     * @param destinationClass
     *            the class to instantiate for the destination
     */
    public BeanPropertiesCopierTools(Object source, Class<?> destinationClass) {
        AssertTools.assertNotNull(destinationClass, "The destinationClass cannot be null");
        Object destination = ReflectionTools.instantiate(destinationClass);
        init(source, destination);
    }

    /**
     * Provide the objects.
     *
     * @param source
     *            the source
     * @param destination
     *            the destination
     */
    public BeanPropertiesCopierTools(Object source, Object destination) {
        init(source, destination);
    }

    /**
     * Copy all the properties with the same name on both side.
     *
     * @return this
     */
    public BeanPropertiesCopierTools copyAllSameProperties() {
        for (PropertyDescriptor propertyDescriptor : sourceWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            try {
                destinationWrapper.setPropertyValue(propertyName, sourceWrapper.getPropertyValue(propertyName));
            } catch (InvalidPropertyException | TypeMismatchException e) {
                // Skip
            }
        }
        return this;
    }

    /**
     * Copy the property.
     *
     * @param propertyName
     *            the name of the source's and destination's property
     * @return this
     */
    public BeanPropertiesCopierTools copyProperty(String propertyName) {
        return copyProperty(propertyName, propertyName);
    }

    /**
     * Copy the property.
     *
     * @param sourcePropertyName
     *            the name of the source's property
     * @param destinationPropertyName
     *            the name of the destination's property
     * @return this
     */
    public BeanPropertiesCopierTools copyProperty(String sourcePropertyName, String destinationPropertyName) {
        Object value = sourceWrapper.getPropertyValue(sourcePropertyName);
        destinationWrapper.setPropertyValue(destinationPropertyName, value);
        return this;
    }

    public Object getDestination() {
        return destinationWrapper.getWrappedInstance();
    }

    public Object getSource() {
        return sourceWrapper.getWrappedInstance();
    }

    /**
     * Get the property's value from the source.
     *
     * @param <T>
     *            the return type
     * @param propertyName
     *            the name of the property
     * @param type
     *            the type to return
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T getSourceProperty(String propertyName, Class<T> type) {
        Object value = sourceWrapper.getPropertyValue(propertyName);
        if (value == null) {
            return null;
        }
        return (T) value;
    }

    private void init(Object source, Object destination) {
        AssertTools.assertNotNull(source, "The source cannot be null");
        AssertTools.assertNotNull(destination, "The destination cannot be null");
        this.sourceWrapper = new BeanWrapperImpl(source);
        this.destinationWrapper = new BeanWrapperImpl(destination);
    }

    /**
     * Set a value on the destination.
     *
     * @param propertyName
     *            the name of the property
     * @param value
     *            the value to set
     */
    public void setDestinationProperty(String propertyName, Object value) {
        destinationWrapper.setPropertyValue(propertyName, value);
    }

    @SuppressWarnings("unchecked")
    public BeanPropertiesCopierTools updateCollection(Collection<?> sourceCollection, String destinationPropertyName) {
        // Get or create the destination
        Object destinationObject = destinationWrapper.getPropertyValue(destinationPropertyName);
        boolean setDestination = destinationObject == null;
        Set<Object> destinationSet = null;
        List<Object> destinationList = null;
        Class<?> destinationPropertyType;
        if (setDestination) {
            destinationPropertyType = destinationWrapper.getPropertyType(destinationPropertyName);
            if (destinationPropertyType == List.class) {
                destinationList = new ArrayList<>();
                destinationObject = destinationList;
            }
            if (destinationPropertyType == Set.class) {
                destinationSet = new HashSet<>();
                destinationObject = destinationSet;
            }
        } else {
            destinationPropertyType = destinationObject.getClass();

            if (destinationObject instanceof List) {
                destinationList = (List<Object>) destinationObject;
            }
            if (destinationObject instanceof Set) {
                destinationSet = (Set<Object>) destinationObject;
            }
        }

        // Check we have a valid destination
        if (destinationList == null && destinationSet == null) {
            throw new SmallToolsException("The destination must be a List or a Set. Is a " + destinationPropertyType.getName());
        }

        // Get the source
        if (sourceCollection == null) {
            sourceCollection = Collections.emptyList();
        }

        // Copy depending on the type
        if (destinationList != null) {
            int destPos = 0;
            for (Object next : sourceCollection) {
                // Skip or insert
                if (destPos >= destinationList.size() || !destinationList.get(destPos).equals(next)) {
                    destinationList.add(destPos, next);
                }

                ++destPos;
            }
            // Remove extra
            while (destPos < destinationList.size()) {
                destinationList.remove(destPos);
            }

        }
        if (destinationSet != null) {
            // Remove
            Iterator<?> destinationIt = destinationSet.iterator();
            while (destinationIt.hasNext()) {
                Object next = destinationIt.next();
                if (!sourceCollection.contains(next)) {
                    destinationIt.remove();
                }
            }

            // Add
            for (Object next : sourceCollection) {
                if (!destinationSet.contains(next)) {
                    destinationSet.add(next);
                }
            }
        }

        // Set the destination
        if (setDestination) {
            destinationWrapper.setPropertyValue(destinationPropertyName, destinationObject);
        }

        return this;
    }

    /**
     * Take or create the collection on the destination and update with the source. This is useful when the destination collection could be a wrapper of the collection to keep track of it (e.g when
     * using Hibernate)
     *
     * @param propertyName
     *            the name of the source's and destination's property
     * @return this
     */
    public BeanPropertiesCopierTools updateCollection(String propertyName) {
        return updateCollection(propertyName, propertyName);
    }

    /**
     * Take or create the collection on the destination and update with the source. This is useful when the destination collection could be a wrapper of the collection to keep track of it (e.g when
     * using Hibernate)
     *
     * @param sourcePropertyName
     *            the name of the source's property
     * @param destinationPropertyName
     *            the name of the destination's property
     * @return this
     */
    public BeanPropertiesCopierTools updateCollection(String sourcePropertyName, String destinationPropertyName) {
        Collection<?> sourceCollection = (Collection<?>) sourceWrapper.getPropertyValue(sourcePropertyName);
        return updateCollection(sourceCollection, destinationPropertyName);
    }
}
