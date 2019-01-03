/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2019 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.db;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.PostConstruct;

import com.foilen.smalltools.hash.HashMd5sum;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.FileTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.StringTools;
import com.foilen.smalltools.trigger.SmoothTrigger;

/**
 * <p>
 * This is a simple DAO that can contain a single entity and it will be persisted to a single json file. Whenever {@link #save(Object)} is called, it will request the save of the file, but it won't be
 * saved right away to prevent over-usage of the disk. It will save to the file 2 seconds after the last {@link #save(Object)} call or maximum 10 seconds after the first {@link #save(Object)} call .
 * </p>
 *
 * <p>
 * Everytime you retrieve the entity, it will be a clone from the underlying entity. That means you can safely modify a saved or retrieved entity without worrying that it will be modifying the same
 * entity in the database or be modified by any other thread that retrieved it. You need to call {@link #save(Object)} to persist any changes.
 * </p>
 *
 * <p>
 * This DAO is not meant for heavy performance.
 * </p>
 *
 * <p>
 * Every actions are done in a cached in-memory entity to ensure to not over-use the disk for reading.
 * </p>
 *
 * Usage:
 * <ol>
 * <li>Create your entity</li>
 * <li>Create a DAO for your entity to persist</li>
 * <li>Instantiate the DAO</li>
 * <li>Use it</li>
 * </ol>
 *
 * <pre>
 * // Create your entity
 * public class TestDbEntity {
 *
 *     private String id;
 *     private int number;
 *
 *     public TestDbEntity() {
 *     }
 *
 *     public TestDbEntity(String id, int number) {
 *         this.id = id;
 *         this.number = number;
 *     }
 *
 *     public String getId() {
 *         return id;
 *     }
 *
 *     public int getNumber() {
 *         return number;
 *     }
 *
 *     public void setId(String id) {
 *         this.id = id;
 *     }
 *
 *     public void setNumber(int number) {
 *         this.number = number;
 *     }
 * }
 *
 * // Create a DAO for your entity to persist
 * public static class TestSingleDao extends AbstractSingleJsonFileDao&lt;TestDbEntity&gt; {
 *
 *     private File dbFile;
 *     private File stagingFile;
 *
 *     public TestSingleDao(File dbFile) {
 *         this.dbFile = dbFile;
 *         this.stagingFile = new File(dbFile.getAbsolutePath() + "_tmp");
 *     }
 *
 *     &#64;Override
 *     protected File getFinalFile() {
 *         return dbFile;
 *     }
 *
 *     &#64;Override
 *     protected File getStagingFile() {
 *         return stagingFile;
 *     }
 *
 *     &#64;Override
 *     protected Class&lt;TestDbEntity&gt; getType() {
 *         return TestDbEntity.class;
 *     }
 *
 * }
 *
 * // Instantiate the DAO
 * TestSingleDao dao = new TestSingleDao(dbFile);
 * dao.init(); // Called automatically if DAO is exposed as a Spring Bean
 *
 * // Use it
 * TestDbEntity entity = dao.load();
 * entity.setNumber(10);
 * dao.save(entity);
 * </pre>
 *
 * <pre>
 * Dependencies:
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.9.1'
 * </pre>
 *
 */
public abstract class AbstractSingleJsonFileDao<T> extends AbstractBasics {

    private String previousMd5sum;
    private T cached;

    protected Runnable saveToFile = () -> {

        String cachedMd5sum = HashMd5sum.hashString(JsonTools.prettyPrint(cached));

        // Check if content is different
        if (StringTools.safeEquals(previousMd5sum, cachedMd5sum)) {
            logger.debug("Content didn't change. Skipping saving");
            return;
        }

        // Save
        logger.debug("Saving to {}", getFinalFile().getAbsolutePath());
        OutputStream out = FileTools.createStagingFile(getStagingFile(), getFinalFile());
        JsonTools.writeToStream(out, cached);
        try {
            out.close();
            previousMd5sum = cachedMd5sum;
        } catch (IOException e) {
            logger.error("Could not close the staging file {}", getStagingFile().getAbsolutePath(), e);
        }

    };

    private SmoothTrigger saveSmoothTrigger = new SmoothTrigger(2000, 10000, false, saveToFile).start();

    /**
     * Save to the file now.
     */
    public synchronized void flush() {
        saveToFile.run();
    }

    /**
     * Tell which file will contain the data.
     *
     * @return the file
     */
    protected abstract File getFinalFile();

    /**
     * Tell which file will be used as a staging file and be renamed to {@link #getFinalFile()} once well written.
     *
     * @return the file
     */
    protected abstract File getStagingFile();

    /**
     * Tell the type of the entity to load and save.
     *
     * @return the type
     */
    protected abstract Class<T> getType();

    /**
     * Call once before using if you are not using Spring.
     */
    @PostConstruct
    public synchronized void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> flush()));
    }

    /**
     * Get the last saved entity.
     *
     * @return the entity
     */
    public T load() {
        if (cached != null) {
            logger.debug("Return cached");
            return JsonTools.clone(cached);
        }
        if (getFinalFile().exists()) {
            logger.debug("Loading from file");
            String json = FileTools.getFileAsString(getFinalFile());
            cached = JsonTools.readFromString(json, getType());
            previousMd5sum = HashMd5sum.hashString(json);
            return JsonTools.readFromString(json, getType());
        } else {
            logger.debug("New state");
            cached = ReflectionTools.instantiate(getType());
            return ReflectionTools.instantiate(getType());
        }
    }

    /**
     * Persist the entity.
     *
     * @param entity
     *            the entity
     */
    public void save(T entity) {
        cached = JsonTools.clone(entity);
        saveSmoothTrigger.request();
    }

}
