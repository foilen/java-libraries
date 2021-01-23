/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.bean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.foilen.smalltools.bean.BeanRepository;

/**
 * Put on the fields to configure with beans in the {@link BeanRepository}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BeanConfigure {

    /**
     * When the field is a collection, you must specify the types of beans in that list.
     *
     * @return the type of the collection
     */
    Class<?> collectionType() default void.class;
}
