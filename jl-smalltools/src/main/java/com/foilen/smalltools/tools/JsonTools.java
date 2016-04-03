/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2016 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.reflection.ReflectionTools;

/**
 * A quick tool to serialize/deserialize to JSON.
 * 
 * <pre>
 * Dependencies:
 * compile 'com.fasterxml.jackson.core:jackson-databind:2.4.5'
 * </pre>
 */
public final class JsonTools {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper NON_FAIL_OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        NON_FAIL_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        NON_FAIL_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        NON_FAIL_OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        NON_FAIL_OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        NON_FAIL_OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Return a pretty print JSON String.
     * 
     * @param object
     *            the object to serialize
     * @return the JSON String
     */
    public static String prettyPrint(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    /**
     * Read the JSON file.
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
        try {
            return OBJECT_MAPPER.readValue(file, clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Read the JSON file inside an already existing object.
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
     * Read the JSON file.
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
     * Read the JSON file inside an already existing object.
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
     * Read the JSON file ignoring some failures.
     * 
     * @param file
     *            the file
     * @param clazz
     *            the type of the final object
     * @param <T>
     *            the type of the final object
     * @return the object
     */
    public static <T> T readFromFileIgnoreFail(File file, Class<T> clazz) {
        try {
            return NON_FAIL_OBJECT_MAPPER.readValue(file, clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Read the JSON file, ignoring some failures, inside an already existing object.
     * 
     * @param file
     *            the file
     * @param target
     *            the already existing object
     */
    public static void readFromFileIgnoreFail(File file, Object target) {
        Object readObject = readFromFileIgnoreFail(file, target.getClass());
        ReflectionTools.copyAllProperties(readObject, target);
    }

    /**
     * Read the JSON file, ignoring some failures, inside an already existing object.
     * 
     * @param fileName
     *            the full path to the file
     * @param target
     *            the already existing object
     */
    public static void readFromFileIgnoreFail(String fileName, Object target) {
        readFromFileIgnoreFail(new File(fileName), target);
    }

    /**
     * Read the JSON resource.
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
        return readFromResource(resource, clazz, JsonTools.class);
    }

    /**
     * Read the JSON resource.
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
            return OBJECT_MAPPER.readValue(context.getResourceAsStream(resource), clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Read the JSON String.
     * 
     * @param text
     *            the json content
     * @param clazz
     *            the type of the final object
     * @param <T>
     *            the type of the final object
     * @return the object
     */
    public static <T> T readFromString(String text, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Write the JSON to the file.
     * 
     * @param file
     *            the file
     * @param object
     *            the object to serialize
     */
    public static void writeToFile(File file, Object object) {
        try {
            OBJECT_MAPPER.writeValue(file, object);
        } catch (IOException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    /**
     * Write the JSON to the file.
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
     * Write the JSON to the stream. The stream will not be closed.
     * 
     * @param stream
     *            the stream
     * @param object
     *            the object to serialize
     */
    public static void writeToStream(OutputStream stream, Object object) {
        try {
            OBJECT_MAPPER.writeValue(stream, object);
        } catch (IOException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    /**
     * Write the JSON to a String.
     * 
     * @param object
     *            the object to serialize
     * @return the JSON string
     */
    public static String writeToString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    private JsonTools() {
    }
}
