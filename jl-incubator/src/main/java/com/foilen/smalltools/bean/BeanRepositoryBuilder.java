/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.bean;

import com.foilen.smalltools.bean.annotation.BeanConfigure;

/**
 * A builder to queue all the beans to create and create a BeanRepository once completely filled. The goal is to be able to insert the beans in any order.
 */
public interface BeanRepositoryBuilder {

    /**
     * When all the desired beans are queued, call this method to create the repository. Will throw an exception if some dependencies are not fulfilled.
     *
     * @return the {@link BeanRepository}
     */
    BeanRepository create();

    /**
     * Will instantiate the class and keep it as is.
     *
     * @param classes
     *            the type(s)
     * @return this - to continue building
     */
    BeanRepositoryBuilder queue(Class<?>... classes);

    /**
     * Keep the objects as is.
     *
     * @param objects
     *            the objects
     * @return this - to continue building
     */
    BeanRepositoryBuilder queue(Object... objects);

    /**
     * Will instantiate the class and wire all the fields that has the {@link BeanConfigure} annotation. All the dependent beans must be already registered.
     *
     * @param classes
     *            the type(s)
     * @return this - to continue building
     */
    BeanRepositoryBuilder queueAndConfig(Class<?>... classes);

    /**
     * Wire all the fields that has the {@link BeanConfigure} annotation. All the dependent beans must be already registered.
     *
     * @param objects
     *            the objects
     * @return this - to continue building
     */
    BeanRepositoryBuilder queueAndConfig(Object... objects);

    /**
     * Queue all the beans of a provider in this builder.
     *
     * @param beanProviderClass
     *            the type of the provider
     * @return this - to continue building
     */
    BeanRepositoryBuilder useProvider(Class<? extends BeansProvider> beanProviderClass);

}
