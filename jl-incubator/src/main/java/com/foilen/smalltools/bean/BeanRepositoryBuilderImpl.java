/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.bean;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.AssertTools;

/**
 * A builder to queue all the beans to create and create a BeanRepository once completely filled. The goal is to be able to insert the beans in any order.
 */
public class BeanRepositoryBuilderImpl implements BeanRepositoryBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleBeanRepository.class);

    private List<Class<?>> classesToInstanciate = new ArrayList<>();
    private List<Class<?>> classesToInstanciateAndConfigure = new ArrayList<>();
    private List<Object> objectsToAdd = new ArrayList<>();
    private List<Object> objectsToAddAndConfigure = new ArrayList<>();

    @Override
    public BeanRepository create() {

        LOG.debug("Starting the creation of BeanRepository");

        BeanRepository beanRepository = new SimpleBeanRepository();
        beanRepository.skipPostConfigMethods(true);

        // Classes
        for (Class<?> clazz : classesToInstanciate) {
            beanRepository.add(clazz);
        }

        // Classes + Config
        List<Object> objectsToConfigure = new ArrayList<>();
        for (Class<?> clazz : classesToInstanciateAndConfigure) {
            objectsToConfigure.add(beanRepository.add(clazz));
        }

        // Objects
        for (Object object : objectsToAdd) {
            beanRepository.add(object);
        }

        // Objects + Config
        for (Object object : objectsToAddAndConfigure) {
            beanRepository.add(object);
        }

        // Configure all
        for (Object object : objectsToConfigure) {
            beanRepository.config(object);
        }
        for (Object object : objectsToAddAndConfigure) {
            beanRepository.config(object);
        }

        beanRepository.skipPostConfigMethods(false);

        // Call the post configure methods
        for (Object object : objectsToConfigure) {
            beanRepository.postConfig(object);
        }
        for (Object object : objectsToAddAndConfigure) {
            beanRepository.postConfig(object);
        }

        return beanRepository;
    }

    @Override
    public BeanRepositoryBuilder queue(Class<?>... classes) {
        AssertTools.assertNotNull(classes, "You cannot queue a null classes list");
        for (Class<?> clazz : classes) {
            AssertTools.assertNotNull(clazz, "You cannot queue a null class");
            LOG.debug("Queuing the class {}", clazz.getName());
            classesToInstanciate.add(clazz);
        }
        return this;
    }

    @Override
    public BeanRepositoryBuilder queue(Object... objects) {
        AssertTools.assertNotNull(objects, "You cannot queue a null objects list");
        for (Object object : objects) {
            AssertTools.assertNotNull(object, "You cannot queue a null object");
            LOG.debug("Queuing an object of class {}", object.getClass().getName());
            objectsToAdd.add(object);
        }
        return this;
    }

    @Override
    public BeanRepositoryBuilder queueAndConfig(Class<?>... classes) {
        AssertTools.assertNotNull(classes, "You cannot queue a null classes list");
        for (Class<?> clazz : classes) {
            AssertTools.assertNotNull(clazz, "You cannot queue a null class");
            LOG.debug("Queuing the class {} and it will be configured", clazz.getName());
            classesToInstanciateAndConfigure.add(clazz);
        }
        return this;
    }

    @Override
    public BeanRepositoryBuilder queueAndConfig(Object... objects) {
        AssertTools.assertNotNull(objects, "You cannot queue a null objects list");
        for (Object object : objects) {
            AssertTools.assertNotNull(object, "You cannot queue a null object");
            LOG.debug("Queuing an object of class {} and it will be configured", object.getClass().getName());
            objectsToAddAndConfigure.add(object);
        }
        return this;
    }

    @Override
    public BeanRepositoryBuilder useProvider(Class<? extends BeansProvider> beanProviderClass) {
        AssertTools.assertNotNull(beanProviderClass, "You cannot queue a null provider");
        BeansProvider beanProvider = ReflectionTools.instantiate(beanProviderClass);
        beanProvider.provide(this);
        return this;
    }

}
