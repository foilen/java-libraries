/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2022 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.foilen.smalltools.bean.annotation.BeanConfigure;
import com.foilen.smalltools.comparator.ClassNameComparator;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.AssertTools;

/**
 * Manages the beans. Can configure outside objects and internal beans.
 */
public class SimpleBeanRepository implements BeanRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleBeanRepository.class);

    private Map<Class<?>, Set<Object>> beansByType = new HashMap<>();
    private SortedSet<Object> beans = new ConcurrentSkipListSet<>(new ClassNameComparator());
    private AtomicBoolean skipPostConfigMethods = new AtomicBoolean(false);

    public SimpleBeanRepository() {
        addBeanInListAndMap(this);
    }

    @Override
    public <T> T add(Class<T> clazz) {
        AssertTools.assertNotNull(clazz, "You cannot add a null class");
        LOG.debug("Adding the class {}", clazz.getName());
        T bean = ReflectionTools.instantiate(clazz);
        addBeanInListAndMap(bean);
        return bean;
    }

    @Override
    public Object add(Object object) {
        AssertTools.assertNotNull(object, "You cannot add a null object");
        LOG.debug("Adding an object of class {}", object.getClass().getName());
        addBeanInListAndMap(object);
        return object;
    }

    @Override
    public <T> T addAndConfig(Class<T> clazz) {
        AssertTools.assertNotNull(clazz, "You cannot add a null class");
        LOG.debug("Adding the class {}", clazz.getName());
        T bean = ReflectionTools.instantiate(clazz);
        addBeanInListAndMap(bean);
        config(bean);
        return bean;
    }

    @Override
    public Object addAndConfig(Object object) {
        AssertTools.assertNotNull(object, "You cannot add a null object");
        LOG.debug("Adding an object of class {}", object.getClass().getName());
        config(object);
        addBeanInListAndMap(object);
        return object;
    }

    /**
     * Add the bean in the list and in the map (with the hierarchies of types.
     *
     * @param bean
     *            the bean
     */
    protected void addBeanInListAndMap(Object bean) {
        beans.add(bean);
        addBeanInMap(bean.getClass(), bean);
    }

    /**
     * Add the bean to the current type and all its super-types.
     *
     * @param clazz
     *            the type
     * @param bean
     *            the bean
     */
    protected void addBeanInMap(Class<?> clazz, Object bean) {

        for (Class<?> currentType : ReflectionTools.allTypes(clazz)) {

            // Get or create the list for the current type
            Set<Object> beansOfCurrentType = beansByType.get(currentType);
            if (beansOfCurrentType == null) {
                beansOfCurrentType = new HashSet<>();
                beansByType.put(currentType, beansOfCurrentType);
            }

            // Add to the list
            beansOfCurrentType.add(bean);
        }
    }

    @Override
    public <T> T config(Class<T> clazz) {
        AssertTools.assertNotNull(clazz, "You cannot configure a null class");
        T object = ReflectionTools.instantiate(clazz);
        config(object);
        return object;
    }

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

    @Override
    public Set<?> getAllBeans() {
        return Collections.unmodifiableSortedSet(beans);
    }

    @Override
    public Set<String> getAllBeansTypesName() {
        Set<String> beansNames = new ConcurrentSkipListSet<>();

        for (Object bean : beans) {
            beansNames.add(bean.getClass().getName());
        }

        return beansNames;
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        Set<T> beans = getBeans(clazz);
        AssertTools.assertTrue(beans.size() == 1, "There must be only one object of type " + clazz.getName() + " . Currently, there are " + beans.size());
        return beans.iterator().next();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> getBeans(Class<T> clazz) {
        AssertTools.assertNotNull(clazz, "You cannot retrieve a null class");
        LOG.debug("Retrieving the beans with the class {}", clazz.getName());

        Set<T> result = (Set<T>) beansByType.get(clazz);
        if (result == null) {
            result = Collections.EMPTY_SET;
        } else {
            result = Collections.unmodifiableSet(result);
        }

        return result;
    }

    @Override
    public Object postConfig(Object object) {
        String className = object.getClass().getName();
        for (Method method : ReflectionTools.allMethodsWithAnnotation(object.getClass(), PostConstruct.class)) {
            String methodName = method.getName();

            LOG.debug("Calling the method {}.{}", className, methodName);
            int amountOfParameters = method.getParameterTypes().length;
            if (amountOfParameters != 0) {
                throw new SmallToolsException("The method " + className + "." + methodName + " has " + amountOfParameters + " parameters, but there must be 0.");
            }
            try {
                method.invoke(object);
            } catch (Exception e) {
                throw new SmallToolsException("The method " + className + "." + methodName + " threw an exception.", e);
            }
        }

        return object;
    }

    @Override
    public void skipPostConfigMethods(boolean skip) {
        skipPostConfigMethods.set(skip);
    }

}
