/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2017 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.spring.resourceresolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.GzipResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.tools.CloseableTools;
import com.foilen.smalltools.tools.CollectionsTools;
import com.foilen.smalltools.tools.CompressionTools;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.ResourceTools;
import com.foilen.smalltools.tools.StreamsTools;
import com.foilen.smalltools.tools.ThreadTools;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A way to automatically bundle multiple files together and even GZIP them.
 * 
 * 
 * Use it in a configuration class that extends {@link WebMvcConfigurerAdapter}.
 * 
 * Ex:
 * 
 * <pre>
 * 
 * &#64;Bean
 * public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
 *     ResourceUrlEncodingFilter resourceUrlEncodingFilter = new ResourceUrlEncodingFilter();
 *     return resourceUrlEncodingFilter;
 * }
 * 
 * &#64;Override
 * public void addResourceHandlers(ResourceHandlerRegistry registry) {
 *     registry.addResourceHandler("/resources/**").addResourceLocations("classpath:/WEB-INF/confignui/resources/");
 *     registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:/WEB-INF/confignui/resources/fonts/");
 * 
 *     boolean isProd = "PROD".equals(System.getProperty("MODE"));
 * 
 *     ResourceChainRegistration chain = registry.addResourceHandler("/bundles/**") //
 *             .setCachePeriod(365 * 24 * 60 * 60) //
 *             .resourceChain(isProd) //
 *             .addResolver(new GzipResourceResolver()); //
 *     if (isProd) {
 *         chain.addResolver(new CachingResourceResolver(new ConcurrentMapCache("bundles")));
 *     }
 *     chain.addResolver(new VersionResourceResolver() //
 *             .addContentVersionStrategy("/**")) //
 *             .addResolver(new BundleResourceResolver() //
 *                     .setCache(isProd) //
 *                     .setGenerateGzip(true) //
 *                     .addBundleResource("all.css", "/WEB-INF/confignui/resources/css/bootstrap-3.3.6.min.css") //
 *                     .addBundleResource("all.css", "/WEB-INF/confignui/resources/css/bootstrap-theme-3.3.6.min.css") //
 *                     .addBundleResource("all.css", "/WEB-INF/confignui/resources/css/ccloud.css") //
 *                     .addBundleResource("all.css", "/WEB-INF/confignui/resources/css/glyphicons.css") //
 *                     .addBundleResource("all.css", "/WEB-INF/confignui/resources/css/glyphicons-bootstrap.css") //
 *                     .addBundleResource("all.js", "/WEB-INF/confignui/resources/js/jquery-1.11.3.min.js") //
 *                     .addBundleResource("all.js", "/WEB-INF/confignui/resources/js/bootstrap-3.3.6.min.js") //
 *                     .addBundleResource("all.js", "/WEB-INF/confignui/resources/js/Chart.bundle.js") //
 *                     .addBundleResource("all.js", "/WEB-INF/confignui/resources/js/ccloud.js") //
 *                     .addBundleResource("all.js", "/WEB-INF/confignui/resources/js/fields.js") //
 *                     .primeCache() //
 * 
 *     );
 * 
 * }
 * </pre>
 * 
 * 
 * You can then use these bundles in your html templater. If using Freemarker:
 * 
 * <pre>
 * &lt;#import "/spring.ftl" as spring /&gt;
 * 
 * &lt;link href="&lt;@spring.url'/bundles/all.css'/&gt;" rel="stylesheet"&gt;
 * &lt;script src="&lt;@spring.url'/bundles/all.js'/&gt;"&gt;&lt;/script&gt;
 * </pre>
 * 
 * <pre>
 * Dependencies:
 * compile 'com.google.guava:guava:18.0'
 * compile 'javax.servlet:javax.servlet-api:3.1.0' 
 * compile 'org.slf4j:slf4j-api:1.7.21'
 * compile 'org.springframework:spring-core:4.1.6.RELEASE'
 * compile 'org.springframework:spring-webmvc:4.1.6.RELEASE'
 * </pre>
 */
public class BundleResourceResolver implements ResourceResolver {

    private class BundleResourceResolverCacheLoader extends CacheLoader<String, Resource> {
        @Override
        public Resource load(String requestPath) throws Exception {
            // Create temporary file
            OutputStream out = null;
            try {
                String extension = FileTools.getExtension(requestPath);
                if (extension == null) {
                    extension = "tmp";
                }
                File tmpFile = File.createTempFile("bundle", "." + extension);
                logger.info("Concatenating the bundle {} in tmp file {}", requestPath, tmpFile.getAbsolutePath());

                out = new FileOutputStream(tmpFile);
                List<String> resources = resourcesByBundleName.get(requestPath);
                for (String resource : resources) {
                    try {
                        StreamsTools.flowStream(ResourceTools.getResourceAsStream(resource), out);
                    } catch (SmallToolsException e) {
                        logger.error("Could not retrieve {} for bundle {}", resource, requestPath, e);
                        return null;
                    }
                }

                // Close and check to generate gzip version
                CloseableTools.close(out);
                if (generateGzip) {
                    String target = tmpFile.getAbsolutePath() + ".gz";
                    logger.info("GZipping the bundle {} in tmp file {}", requestPath, target);
                    CompressionTools.gzipFileToFile(tmpFile, target);
                }

                return new FileSystemResource(tmpFile);

            } catch (IOException e) {
                logger.error("Could not create a temporary file for bundle {}", requestPath, e);
                return null;
            } finally {
                CloseableTools.close(out);
            }
        }
    }

    private final static Logger logger = LoggerFactory.getLogger(BundleResourceResolver.class);

    private Map<String, List<String>> resourcesByBundleName = new HashMap<>();
    private LoadingCache<String, Resource> cachedResources;
    private boolean generateGzip = false;

    public BundleResourceResolver() {
        setCache(false);
    }

    public BundleResourceResolver addBundleResource(String bundleName, String absoluteResourcePath) {
        List<String> resources = CollectionsTools.getOrCreateEmptyArrayList(resourcesByBundleName, bundleName, String.class);
        resources.add(absoluteResourcePath);
        return this;
    }

    /**
     * If you are using cache, a thread will run now to prepare all the bundles that are already defined.
     * 
     * @return this
     */
    public BundleResourceResolver primeCache() {
        if (cachedResources == null) {
            logger.warn("You are requesting to prime the cache now, but the caching is not enabled");
        }

        Thread thread = new Thread(() -> {
            ThreadTools.nameThread().clear().appendText("Prime the cache - Started at - ").appendDate().change();
            logger.info("[BEGIN] Prime the cache");

            for (String bundleName : resourcesByBundleName.keySet()) {
                try {
                    logger.info("Priming {}", bundleName);
                    cachedResources.get(bundleName);
                } catch (ExecutionException e) {
                    logger.error("Got an error while priming {}", bundleName);
                }
            }

            logger.info("[END] Prime the cache");
        });
        thread.setDaemon(true);
        thread.start();

        return this;
    }

    @Override
    public Resource resolveResource(HttpServletRequest request, String requestPath, List<? extends Resource> locations, ResourceResolverChain chain) {

        // Check if exists
        List<String> resources = resourcesByBundleName.get(requestPath);
        if (resources == null) {
            return chain.resolveResource(request, requestPath, locations);
        }

        try {
            return cachedResources.get(requestPath);
        } catch (ExecutionException e) {
            logger.error("Could not retrieve the bundle {}", requestPath, e);
            return null;
        }

    }

    @Override
    public String resolveUrlPath(String resourcePath, List<? extends Resource> locations, ResourceResolverChain chain) {
        if (resourcesByBundleName.containsKey(resourcePath)) {
            return resourcePath;
        }

        return chain.resolveUrlPath(resourcePath, locations);
    }

    /**
     * Activate the cache if you know that the file won't change. (E.g in production)
     * 
     * @param cache
     *            true to activate the caching
     * @return this
     */
    public BundleResourceResolver setCache(boolean cache) {
        if (cache) {
            cachedResources = CacheBuilder.newBuilder().build(new BundleResourceResolverCacheLoader());
        } else {
            cachedResources = CacheBuilder.newBuilder().maximumSize(0).build(new BundleResourceResolverCacheLoader());
        }
        return this;
    }

    /**
     * After concatenating the files, you can also generate a Gzipped version of it. Useful with {@link GzipResourceResolver}.
     * 
     * @param generateGzip
     *            true to generate a gzip file as well
     * @return this
     */
    public BundleResourceResolver setGenerateGzip(boolean generateGzip) {
        this.generateGzip = generateGzip;
        return this;
    }
}