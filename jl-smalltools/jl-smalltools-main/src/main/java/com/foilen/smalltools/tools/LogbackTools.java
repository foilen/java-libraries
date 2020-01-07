/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2020 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * Some tools for Logback.
 *
 * <pre>
 * Dependencies:
 * compile 'ch.qos.logback:logback-classic:1.2.3'
 * </pre>
 */
public class LogbackTools {

    /**
     * Reset the loggers with the new resource config.
     *
     * @param configResourceName
     *            the name of the config file
     */
    public static void changeConfig(String configResourceName) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(LogbackTools.class.getResourceAsStream(configResourceName));
        } catch (JoranException je) {
            throw new RuntimeException(je);
        }
    }

    private LogbackTools() {
    }

}
