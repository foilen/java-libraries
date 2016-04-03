/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.bean;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.bean.annotation.BeanConfigure;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.AssertTools;

/**
 * This {@link BeanRepository} has 2 of them:
 * 
 * <pre>
 * - A {@link SimpleBeanRepository} that contains the beans created with this {@link HierarchicalBeanRepository}
 * - A {@link BeanRepository} to which it will delegate retrieval to if the current level does not contain any requested bean
 * </pre>
 *
 */
public class HierarchicalBeanRepository implements BeanRepository {

    private static final Logger LOG = LoggerFactory.getLogger(HierarchicalBeanRepository.class);

    private BeanRepository parentBeanRepository;
    private SimpleBeanRepository currentBeanRepository = new SimpleBeanRepository();

    private AtomicBoolean skipPostConfigMethods = new AtomicBoolean(false);

    public HierarchicalBeanRepository(BeanRepository parentBeanRepository) {
        this.parentBeanRepository = parentBeanRepository;
    }

    @Override
    public <T> T add(Class<T> clazz) {
        return currentBeanRepository.add(clazz);
    }

    @Override
    public Object add(Object object) {
        return currentBeanRepository.add(object);
    }

    /**
     * {@inheritDoc} Configure with the current level unless empty ; will configure with parent in that case.
     */
    @Override
    public <T> T addAndConfig(Class<T> clazz) {
        T object = currentBeanRepository.add(clazz);
        return config(object);
    }

    /**
     * {@inheritDoc} Configure with the current level unless empty ; will configure with parent in that case.
     */
    @Override
    public Object addAndConfig(Object object) {
        currentBeanRepository.add(object);
        return config(object);
    }

    /**
     * {@inheritDoc} Configure with the current level unless empty ; will configure with parent in that case.
     */
    @Override
    public <T> T config(Class<T> clazz) {
        AssertTools.assertNotNull(clazz, "You cannot configure a null class");
        T object = ReflectionTools.instantiate(clazz);
        config(object);
        return object;
    }

    /**
     * {@inheritDoc} Configure with the current level unless empty ; will configure with parent in that case.
     */
    @Override
    public <T> T config(T object) {
        AssertTools.assertNotNull(object, "You cannot configure a null object");
        LOG.debug("Configuring an object of class {}", object.getClass().getName());

        // Fill all the fields
        for (Field field : ReflectionTools.allFieldsWithAnnotation(object.getClass(), BeanConfigure.class)) {
            // Details
            Class<?> valueType = field.getType();
            String fieldName = field.getName();
            LOG.debug("Configuring the field {} with a value of type {}", fieldName, valueType);

            // Force accessibility
            field.setAccessible(true);

            // Set the value
            boolean isCollection = Collection.class.isAssignableFrom(valueType);
            try {
                if (isCollection) {
                    // Get the type of the collection
                    valueType = field.getAnnotation(BeanConfigure.class).collectionType();
                    AssertTools.assertFalse(valueType.equals(void.class), "When a field is a collection, you must specify a 'collectionType'");

                    LOG.debug("Is a collection of type {}", valueType);
                    field.set(object, getBeans(valueType));
                } else {
                    LOG.debug("Is a single value");
                    field.set(object, getBean(valueType));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new SmallToolsException("Could not set the field " + fieldName, e);
            }
        }

        // Execute all the post config methods
        if (!skipPostConfigMethods.get()) {
            postConfig(object);
        }

        return object;
    }

    /**
     * {@inheritDoc} Only on the current level.
     */
    @Override
    public Set<?> getAllBeans() {
        return currentBeanRepository.getAllBeans();
    }

    /**
     * {@inheritDoc} Only on the current level.
     */
    @Override
    public Set<String> getAllBeansTypesName() {
        return currentBeanRepository.getAllBeansTypesName();
    }

    /**
     * {@inheritDoc} From the current level unless empty ; will get from parent in that case.
     */
    @Override
    public <T> T getBean(Class<T> clazz) {
        Set<T> beans = getBeans(clazz);
        AssertTools.assertTrue(beans.size() == 1, "There must be only one object of type " + clazz.getName() + " . Currently, there are " + beans.size());
        return beans.iterator().next();
    }

    /**
     * {@inheritDoc} From the current level unless empty ; will get from parent in that case.
     */
    @Override
    public <T> Set<T> getBeans(Class<T> clazz) {
        Set<T> beans = currentBeanRepository.getBeans(clazz);
        if (beans.isEmpty()) {
            beans = parentBeanRepository.getBeans(clazz);
        }
        return beans;
    }

    public BeanRepository getParentBeanRepository() {
        return parentBeanRepository;
    }

    @Override
    public Object postConfig(Object object) {
        return currentBeanRepository.postConfig(object);
    }

    public void setParentBeanRepository(BeanRepository parentBeanRepository) {
        this.parentBeanRepository = parentBeanRepository;
    }

    @Override
    public void skipPostConfigMethods(boolean skip) {
        skipPostConfigMethods.set(skip);
        currentBeanRepository.skipPostConfigMethods(skip);
        parentBeanRepository.skipPostConfigMethods(skip);
    }

}
