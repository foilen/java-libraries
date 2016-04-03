/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;
import org.springframework.util.Assert;

/**
 * Configure beans outside of the container.
 * 
 * Usage:
 * 
 * <pre>
 * - Add this as a bean in your Spring application.
 * - Use SpringTools.configure(yourObject) to configure it with your Spring container.
 * </pre>
 * 
 * <pre>
* Dependencies:
* compile 'org.springframework:spring-beans:4.1.6.RELEASE'
 * </pre>
 */
public class SpringTools {

    public static void configure(Object object) {
        Assert.notNull(instance, "The SpringConfigurer is not yet inside a Spring container");
        instance.beanConfigurerSupport.configureBean(object);
    }

    private static SpringTools instance;

    private BeanConfigurerSupport beanConfigurerSupport;

    @Autowired
    private BeanFactory beanFactory;

    @PostConstruct
    public void init() {
        instance = this;

        beanConfigurerSupport = new BeanConfigurerSupport();
        beanConfigurerSupport.setBeanFactory(beanFactory);
    }

}
