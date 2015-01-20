/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.bean;

import java.util.Set;

import javax.annotation.PostConstruct;

import com.foilen.smalltools.bean.annotation.BeanConfigure;

/**
 * Manages the beans. Can configure outside objects and internal beans.
 */
public interface BeanRepository {

    /**
     * Will instantiate the class and keep it as is.
     * 
     * @param clazz
     *            the type
     */
    <T> T add(Class<T> clazz);

    /**
     * Keep the object as is.
     * 
     * @param object
     *            the object
     */
    Object add(Object object);

    /**
     * Will instantiate the class and wire all the fields that has the {@link BeanConfigure} annotation. Will throw an exception if some dependencies are not already registered.
     * 
     * @param clazz
     *            the type
     */
    <T> T addAndConfig(Class<T> clazz);

    /**
     * Wire all the fields that has the {@link BeanConfigure} annotation. Will throw an exception if some dependencies are not already registered.
     * 
     * @param object
     *            the object
     */
    Object addAndConfig(Object object);

    /**
     * Will only instantiate the class and wire all the fields that has the {@link BeanConfigure} annotation (it won't be added to the repository). Will throw an exception if some dependencies are not
     * already registered.
     * 
     * @param clazz
     *            the type
     * @return the configured object
     */
    <T> T config(Class<T> clazz);

    /**
     * Only wires all the fields that has the {@link BeanConfigure} annotation (it won't be added to the repository). Will throw an exception if some dependencies are not already registered. Also does
     * a {@link #postConfig(Object)} if not skipping that step.
     * 
     * @param object
     *            the object
     * @return the configured object
     */
    <T> T config(T object);

    /**
     * Get all the beans that are registered.
     * 
     * @return the list
     */
    Set<?> getAllBeans();

    /**
     * Get all the bean's types that are registered.
     * 
     * @return the list of types
     */
    Set<String> getAllBeansTypesName();

    /**
     * Get a single bean of the desired type. If not exactly one available, it will throw an exception.
     * 
     * @param clazz
     * @return the bean
     */
    <T> T getBean(Class<T> clazz);

    /**
     * Get a list of beans of the desired type. If none, will return an empty list.
     * 
     * @param clazz
     * @return the beans
     */
    <T> Set<T> getBeans(Class<T> clazz);

    /**
     * Call all the {@link PostConstruct} methods.
     * 
     * @param object
     *            the object
     * @return the same object
     */
    Object postConfig(Object object);

    /**
     * Tell to skip or not the {@link #postConfig(Object)} phase of {@link #config(Class)} and {@link #config(Object)}.
     * 
     * @param skip
     *            true to skip the step
     */
    void skipPostConfigMethods(boolean skip);

}
