package com.foilen.smalltools.tools;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;
import org.springframework.util.Assert;

/**
 * Configure beans outside of the container.
 * <p>
 * Usage:
 *
 * <pre>
 * - Add this as a bean in your Spring application.
 * - Use SpringTools.configure(yourObject) to configure it with your Spring container.
 * </pre>
 */
public class SpringTools {

    private static SpringTools instance;

    /**
     * Configure the object with the Spring container.
     *
     * @param object the object to configure
     */
    public static void configure(Object object) {
        Assert.notNull(instance, "The SpringConfigurer is not yet inside a Spring container");
        instance.beanConfigurerSupport.configureBean(object);
    }

    private BeanConfigurerSupport beanConfigurerSupport;

    @Autowired
    private BeanFactory beanFactory;

    /**
     * Initialize the instance.
     */
    @PostConstruct
    public void init() {
        instance = this;

        beanConfigurerSupport = new BeanConfigurerSupport();
        beanConfigurerSupport.setBeanFactory(beanFactory);
    }

}
