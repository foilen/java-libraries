/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2021 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.hibernate.MappingException;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.util.ClassUtils;

/**
 * To generate the SQL file:
 *
 * <pre>
 * Hibernate4Tools.generateSqlSchema(MySQL5InnoDBDialect.class, "sql/mysql.sql", true, "com.foilen.confignui.db.domain");
 * </pre>
 *
 * <pre>
 * Dependencies:
 * compile 'org.hibernate:hibernate-core:4.3.11.Final'
 * compile 'org.springframework:spring-orm:4.3.11.RELEASE'
 * </pre>
 */
public final class Hibernate4Tools {

    private static final String RESOURCE_PATTERN = "/**/*.class";
    private static final String PACKAGE_INFO_SUFFIX = ".package-info";
    private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private static final Set<TypeFilter> entityTypeFilters;

    static {
        entityTypeFilters = new LinkedHashSet<TypeFilter>(4);
        entityTypeFilters.add(new AnnotationTypeFilter(Entity.class, false));
        entityTypeFilters.add(new AnnotationTypeFilter(Embeddable.class, false));
        entityTypeFilters.add(new AnnotationTypeFilter(MappedSuperclass.class, false));
        try {
            @SuppressWarnings("unchecked")
            Class<? extends Annotation> converterAnnotation = (Class<? extends Annotation>) LocalSessionFactoryBuilder.class.getClassLoader().loadClass("javax.persistence.Converter");
            entityTypeFilters.add(new AnnotationTypeFilter(converterAnnotation, false));
        } catch (ClassNotFoundException ex) {
            // JPA 2.1 API not available - Hibernate <4.3
        }
    }

    /**
     * Generate the SQL file. This is based on the code in {@link LocalSessionFactoryBuilder#scanPackages(String...)}
     *
     * @param dialect
     *            the dialect (e.g: org.hibernate.dialect.MySQL5InnoDBDialect )
     * @param outputSqlFile
     *            where to put the generated SQL file
     * @param useUnderscore
     *            true: to have tables names like "employe_manager" ; false: to have tables names like "employeManager"
     * @param packagesToScan
     *            the packages where your entities are
     */
    @SuppressWarnings("deprecation")
    public static void generateSqlSchema(Class<? extends Dialect> dialect, String outputSqlFile, boolean useUnderscore, String... packagesToScan) {

        // Configuration
        Configuration configuration = new Configuration();
        if (useUnderscore) {
            configuration.setNamingStrategy(new ImprovedNamingStrategy());
        }

        Properties properties = new Properties();
        properties.setProperty(AvailableSettings.DIALECT, dialect.getName());

        // Scan packages
        Set<String> classNames = new TreeSet<String>();
        Set<String> packageNames = new TreeSet<String>();
        try {
            for (String pkg : packagesToScan) {
                String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(pkg) + RESOURCE_PATTERN;
                Resource[] resources = resourcePatternResolver.getResources(pattern);
                MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                for (Resource resource : resources) {
                    if (resource.isReadable()) {
                        MetadataReader reader = readerFactory.getMetadataReader(resource);
                        String className = reader.getClassMetadata().getClassName();
                        if (matchesEntityTypeFilter(reader, readerFactory)) {
                            classNames.add(className);
                        } else if (className.endsWith(PACKAGE_INFO_SUFFIX)) {
                            packageNames.add(className.substring(0, className.length() - PACKAGE_INFO_SUFFIX.length()));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new MappingException("Failed to scan classpath for unlisted classes", ex);
        }
        try {
            for (String className : classNames) {
                configuration.addAnnotatedClass(resourcePatternResolver.getClassLoader().loadClass(className));
            }
            for (String packageName : packageNames) {
                configuration.addPackage(packageName);
            }
        } catch (ClassNotFoundException ex) {
            throw new MappingException("Failed to load annotated classes from classpath", ex);
        }

        // Exportation
        SchemaExport schemaExport = new SchemaExport(configuration, properties);
        schemaExport.setOutputFile(outputSqlFile);
        schemaExport.setDelimiter(";");
        schemaExport.setFormat(true);
        schemaExport.execute(true, false, false, true);
    }

    /**
     * Check whether any of the configured entity type filters matches the current class descriptor contained in the metadata reader.
     */
    private static boolean matchesEntityTypeFilter(MetadataReader reader, MetadataReaderFactory readerFactory) throws IOException {
        for (TypeFilter filter : entityTypeFilters) {
            if (filter.match(reader, readerFactory)) {
                return true;
            }
        }
        return false;
    }

    private Hibernate4Tools() {

    }

}