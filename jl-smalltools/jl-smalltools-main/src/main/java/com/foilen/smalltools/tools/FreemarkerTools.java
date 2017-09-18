/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.StringWriter;
import java.util.Map;

import com.foilen.smalltools.exception.SmallToolsException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * Use the Freemarker templates easily.
 *
 * <pre>
 * Dependencies:
 * compile 'org.freemarker:freemarker:2.3.23'
 * </pre>
 */
public final class FreemarkerTools {

    private static final Configuration freemarkerConfiguration;

    static {
        freemarkerConfiguration = new Configuration(Configuration.VERSION_2_3_22);
        freemarkerConfiguration.setClassForTemplateLoading(FreemarkerTools.class, "/");
        freemarkerConfiguration.setDefaultEncoding(CharsetTools.UTF_8.name());
        freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    /**
     * Take a Freemarker template and get a String from it.
     *
     * @param resource
     *            the path of the resource
     * @param model
     *            the variables available in the templates
     * @return this
     */
    public static String processTemplate(String resource, Map<String, ?> model) {
        try {
            Template template = freemarkerConfiguration.getTemplate(resource);
            StringWriter stringWriter = new StringWriter();
            template.process(model, stringWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            throw new SmallToolsException("Problem generating the file", e);
        }
    }

    private FreemarkerTools() {
    }
}
