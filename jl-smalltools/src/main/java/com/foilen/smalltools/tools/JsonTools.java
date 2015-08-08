/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT
    
 */
package com.foilen.smalltools.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.reflection.ReflectionUtils;

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

    static {
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

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
        ReflectionUtils.copyAllProperties(readObject, target);
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

    private JsonTools() {
    }
}
