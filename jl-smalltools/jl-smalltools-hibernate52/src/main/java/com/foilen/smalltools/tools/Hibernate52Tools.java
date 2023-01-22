/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.util.EnumSet;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;

/**
 * To generate the SQL file:
 *
 * <pre>
 * System.setProperty("hibernate.dialect.storage_engine", "innodb");
 * Hibernate5Tools.generateSqlSchema(MySQL5InnoDBDialect.class, "sql/mysql.sql", true, "com.foilen.confignui.db.domain");
 * </pre>
 *
 * WARNING: Some hibernate naming changed from 4 to 5:
 * <ul>
 * <li>@ElementCollection will generate xxxxx_id instead of only xxxxx</li>
 * <li>@OneToMany table name will have the type name in it</li>
 * <li>Some constraints names will change</li>
 * </ul>
 */
public final class Hibernate52Tools {

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
    public static void generateSqlSchema(Class<? extends Dialect> dialect, String outputSqlFile, boolean useUnderscore, String... packagesToScan) {

        BootstrapServiceRegistry bootstrapServiceRegistry = new BootstrapServiceRegistryBuilder().build();

        MetadataSources metadataSources = new MetadataSources(bootstrapServiceRegistry);

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Embeddable.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(MappedSuperclass.class));
        for (String pkg : packagesToScan) {
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(pkg)) {
                metadataSources.addAnnotatedClassName(beanDefinition.getBeanClassName());
            }
        }

        StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder(bootstrapServiceRegistry);
        standardServiceRegistryBuilder.applySetting(AvailableSettings.DIALECT, dialect.getName());
        StandardServiceRegistryImpl ssr = (StandardServiceRegistryImpl) standardServiceRegistryBuilder.build();
        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder(ssr);

        if (useUnderscore) {
            metadataBuilder.applyImplicitNamingStrategy(new SpringImplicitNamingStrategy());
            metadataBuilder.applyPhysicalNamingStrategy(new SpringPhysicalNamingStrategy());
        }

        new SchemaExport() //
                .setHaltOnError(true) //
                .setOutputFile(outputSqlFile) //
                .setFormat(true) //
                .setDelimiter(";") //
                .execute(EnumSet.of(TargetType.SCRIPT), SchemaExport.Action.CREATE, metadataBuilder.build());

    }

    private Hibernate52Tools() {

    }

}