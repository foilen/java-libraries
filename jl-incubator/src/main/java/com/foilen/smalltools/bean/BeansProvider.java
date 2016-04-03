/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.bean;

/**
 * Instead of inserting one bean at a time in a {@link BeanRepositoryBuilder}, you can add multiple as a configuration.
 */
public interface BeansProvider {

    /**
     * All the beans you want to add to the repository will be added in this method.
     * 
     * @param beanRepositoryBuilder
     *            the builder to fill
     */
    void provide(BeanRepositoryBuilder beanRepositoryBuilder);

}
