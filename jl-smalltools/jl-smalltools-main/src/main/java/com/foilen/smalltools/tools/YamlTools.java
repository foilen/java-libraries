/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.reflection.ReflectionTools;

/**
 * A quick tool to serialize/deserialize to Yaml.
 */
public final class YamlTools {

    private static final Yaml COMPACT_YAML;
    private static final Yaml PRETTY_YAML;

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setPrettyFlow(true);
        PRETTY_YAML = new Yaml(dumperOptions);
        COMPACT_YAML = new Yaml();
    }

    /**
     * Serialize to YAML and deserialize back as a new object.
     *
     * @param <T>
     *            the class of the object
     * @param object
     *            the object to clone
     * @return the new cloned object
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T object) {
        if (object == null) {
            return null;
        }
        Class<? extends Object> type = object.getClass();
        String yaml = compactPrint(object);
        return (T) readFromString(yaml, type);
    }

    /**
     * Return a compact print Yaml String.
     *
     * @param object
     *            the object to serialize
     * @return the Yaml String
     */
    public static String compactPrint(Object object) {
        try {
            return COMPACT_YAML.dump(object);
        } catch (YAMLException e) {
            throw new SmallToolsException("Problem serializing in Yaml", e);
        }
    }

    /**
     * Return a pretty print Yaml String.
     *
     * @param object
     *            the object to serialize
     * @return the Yaml String
     */
    public static String prettyPrint(Object object) {
        try {
            return PRETTY_YAML.dump(object);
        } catch (YAMLException e) {
            throw new SmallToolsException("Problem serializing in Yaml", e);
        }
    }

    /**
     * Read the Yaml file.
     *
     * @param file
     *            the file
     * @param clazz
     *            the type of the final object
     * @param <T>
     *            the type of the final object
     * @return the object
     */
    public static <T> T readFromFile(File file, Class<T> clazz) {
        try (Reader reader = new FileReader(file)) {
            return PRETTY_YAML.loadAs(reader, clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from Yaml", e);
        }
    }

    /**
     * Read the Yaml file inside an already existing object.
     *
     * @param file
     *            the file
     * @param target
     *            the already existing object
     */
    public static void readFromFile(File file, Object target) {
        Object readObject = readFromFile(file, target.getClass());
        ReflectionTools.copyAllProperties(readObject, target);
    }

    /**
     * Read the Yaml file.
     *
     * @param fileName
     *            the full path to the file
     * @param clazz
     *            the type of the final object
     * @param <T>
     *            the type of the final object
     * @return the object
     */
    public static <T> T readFromFile(String fileName, Class<T> clazz) {
        return readFromFile(new File(fileName), clazz);
    }

    /**
     * Read the Yaml file inside an already existing object.
     *
     * @param fileName
     *            the full path to the file
     * @param target
     *            the already existing object
     */
    public static void readFromFile(String fileName, Object target) {
        readFromFile(new File(fileName), target);
    }

    /**
     * Read the Yaml resource.
     *
     * @param resource
     *            the resource to open
     * @param clazz
     *            the type of the final object
     * @param <T>
     *            the type of the final object
     * @return the object
     */
    public static <T> T readFromResource(String resource, Class<T> clazz) {
        return readFromResource(resource, clazz, YamlTools.class);
    }

    /**
     * Read the Yaml resource.
     *
     * @param resource
     *            the resource to open
     * @param clazz
     *            the type of the final object
     * @param context
     *            the context class to use relative path
     * @param <T>
     *            the type of the final object
     * @return the object
     */
    public static <T> T readFromResource(String resource, Class<T> clazz, Class<?> context) {
        try {
            return PRETTY_YAML.loadAs(context.getResourceAsStream(resource), clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from Yaml", e);
        }
    }

    /**
     * Read the Yaml String.
     *
     * @param text
     *            the yaml content
     * @param clazz
     *            the type of the final object
     * @param <T>
     *            the type of the final object
     * @return the object
     */
    public static <T> T readFromString(String text, Class<T> clazz) {
        try {
            return PRETTY_YAML.loadAs(text, clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from Yaml", e);
        }
    }

    /**
     * Write the Yaml to the file.
     *
     * @param file
     *            the file
     * @param object
     *            the object to serialize
     */
    public static void writeToFile(File file, Object object) {
        try (FileWriter output = new FileWriter(file)) {
            PRETTY_YAML.dump(object, output);
        } catch (IOException e) {
            throw new SmallToolsException("Problem serializing in Yaml", e);
        }
    }

    /**
     * Write the Yaml to the file.
     *
     * @param fileName
     *            the full path to the file
     * @param object
     *            the object to serialize
     */
    public static void writeToFile(String fileName, Object object) {
        writeToFile(new File(fileName), object);
    }

    /**
     * Write the Yaml to the stream. The stream will not be closed.
     *
     * @param stream
     *            the stream
     * @param object
     *            the object to serialize
     */
    public static void writeToStream(OutputStream stream, Object object) {
        try {
            PRETTY_YAML.dump(object, new OutputStreamWriter(stream));
        } catch (Exception e) {
            throw new SmallToolsException("Problem serializing in Yaml", e);
        }
    }

    /**
     * Write the Yaml to a String.
     *
     * @param object
     *            the object to serialize
     * @return the Yaml string
     */
    public static String writeToString(Object object) {
        try {
            return PRETTY_YAML.dump(object);
        } catch (YAMLException e) {
            throw new SmallToolsException("Problem serializing in Yaml", e);
        }
    }

    private YamlTools() {
    }
}
