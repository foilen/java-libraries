package com.foilen.smalltools.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.reflection.ReflectionTools;

/**
 * A quick tool to serialize/deserialize to JSON.
 */
public final class JsonTools {

    private static final ObjectMapper COMPACT_OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper COMPACT_SKIPNULL_OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper PRETTY_OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper PRETTY_SKIPNULL_OBJECT_MAPPER = new ObjectMapper();
    private static final ObjectMapper NON_FAIL_OBJECT_MAPPER = new ObjectMapper();

    static {
        PRETTY_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        PRETTY_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        PRETTY_OBJECT_MAPPER.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        PRETTY_OBJECT_MAPPER.registerModule(new JavaTimeModule());

        PRETTY_SKIPNULL_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        PRETTY_SKIPNULL_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        PRETTY_SKIPNULL_OBJECT_MAPPER.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        PRETTY_SKIPNULL_OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
        PRETTY_SKIPNULL_OBJECT_MAPPER.registerModule(new JavaTimeModule());

        COMPACT_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        COMPACT_OBJECT_MAPPER.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        COMPACT_OBJECT_MAPPER.registerModule(new JavaTimeModule());

        COMPACT_SKIPNULL_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        COMPACT_SKIPNULL_OBJECT_MAPPER.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        COMPACT_SKIPNULL_OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
        COMPACT_SKIPNULL_OBJECT_MAPPER.registerModule(new JavaTimeModule());

        NON_FAIL_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        NON_FAIL_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        NON_FAIL_OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        NON_FAIL_OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        NON_FAIL_OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        NON_FAIL_OBJECT_MAPPER.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        NON_FAIL_OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    /**
     * Serialize to JSON and deserialize back as a new object of the specified class.
     *
     * @param object the object to clone
     * @param clazz  the type of the final object
     * @param <T>    the type of the final object
     * @return the new cloned object
     */
    public static <T> T clone(Object object, Class<T> clazz) {
        if (object == null) {
            return null;
        }
        String json = compactPrint(object);
        return readFromString(json, clazz);
    }

    /**
     * Serialize to JSON and deserialize back as a new object.
     *
     * @param <T>    the class of the object
     * @param object the object to clone
     * @return the new cloned object
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T object) {
        if (object == null) {
            return null;
        }
        Class<? extends Object> type = object.getClass();
        String json = compactPrint(object);
        return (T) readFromString(json, type);
    }

    /**
     * Serialize to JSON and deserialize back as a new SortedMap with all sub-objects as SortedMap.
     *
     * @param <T>    the class of the object
     * @param object the object to clone
     * @return the new cloned object
     */
    @SuppressWarnings("unchecked")
    public static <T> SortedMap<String, Object> cloneAsSortedMap(T object) {
        if (object == null) {
            return null;
        }
        SortedMap<String, Object> sortedMap = JsonTools.clone(object, SortedMap.class);
        convertAllMapsToSortedMap(sortedMap);
        return sortedMap;
    }

    /**
     * Return a compact print JSON String.
     *
     * @param object the object to serialize
     * @return the JSON String
     */
    public static String compactPrint(Object object) {
        try {
            return COMPACT_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    /**
     * Return a compact print JSON String and ignore all null values.
     *
     * @param object the object to serialize
     * @return the JSON String
     */
    public static String compactPrintWithoutNulls(Object object) {
        try {
            return COMPACT_SKIPNULL_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void convertAllMapsToSortedMap(SortedMap<String, Object> sortedMap) {

        for (String key : sortedMap.keySet().stream().collect(Collectors.toList())) {
            Object value = sortedMap.get(key);
            if (value instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>) value;
                SortedMap<String, Object> subSortedMap = new TreeMap<>(subMap);
                sortedMap.put(key, subSortedMap);
                convertAllMapsToSortedMap(subSortedMap);
            } else if (value instanceof List) {
                List<Object> subList = (List<Object>) value;
                for (int i = 0; i < subList.size(); ++i) {
                    Object item = subList.get(i);
                    if (item instanceof Map) {
                        subList.set(i, new TreeMap<>((Map<String, Object>) item));
                    }
                }
            }
        }

    }

    /**
     * Return a pretty print JSON String.
     *
     * @param object the object to serialize
     * @return the JSON String
     */
    public static String prettyPrint(Object object) {
        try {
            return PRETTY_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    /**
     * Return a pretty print JSON String and ignore all null values.
     *
     * @param object the object to serialize
     * @return the JSON String
     */
    public static String prettyPrintWithoutNulls(Object object) {
        try {
            return PRETTY_SKIPNULL_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    /**
     * Read the JSON file.
     *
     * @param file  the file
     * @param clazz the type of the final object
     * @param <T>   the type of the final object
     * @return the object
     */
    public static <T> T readFromFile(File file, Class<T> clazz) {
        try {
            return PRETTY_OBJECT_MAPPER.readValue(file, clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Read the JSON file inside an already existing object.
     *
     * @param file   the file
     * @param target the already existing object
     */
    public static void readFromFile(File file, Object target) {
        Object readObject = readFromFile(file, target.getClass());
        ReflectionTools.copyAllProperties(readObject, target);
    }

    /**
     * Read the JSON file.
     *
     * @param fileName the full path to the file
     * @param clazz    the type of the final object
     * @param <T>      the type of the final object
     * @return the object
     */
    public static <T> T readFromFile(String fileName, Class<T> clazz) {
        return readFromFile(new File(fileName), clazz);
    }

    /**
     * Read the JSON file inside an already existing object.
     *
     * @param fileName the full path to the file
     * @param target   the already existing object
     */
    public static void readFromFile(String fileName, Object target) {
        readFromFile(new File(fileName), target);
    }

    /**
     * Read the JSON file.
     *
     * @param file  the file
     * @param clazz the type of the final object
     * @param <T>   the type of the final object
     * @return the list of objects
     */
    public static <T> List<T> readFromFileAsList(File file, Class<T> clazz) {
        try {
            CollectionType listType = PRETTY_OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
            return PRETTY_OBJECT_MAPPER.readValue(file, listType);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Read the JSON file.
     *
     * @param fileName the full path to the file
     * @param clazz    the type of the final object
     * @param <T>      the type of the final object
     * @return the list of objects
     */
    public static <T> List<T> readFromFileAsList(String fileName, Class<T> clazz) {
        return readFromFileAsList(new File(fileName), clazz);
    }

    /**
     * Read the JSON file ignoring some failures.
     *
     * @param file  the file
     * @param clazz the type of the final object
     * @param <T>   the type of the final object
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
     * @param file   the file
     * @param target the already existing object
     */
    public static void readFromFileIgnoreFail(File file, Object target) {
        Object readObject = readFromFileIgnoreFail(file, target.getClass());
        ReflectionTools.copyAllProperties(readObject, target);
    }

    /**
     * Read the JSON file, ignoring some failures, inside an already existing object.
     *
     * @param fileName the full path to the file
     * @param target   the already existing object
     */
    public static void readFromFileIgnoreFail(String fileName, Object target) {
        readFromFileIgnoreFail(new File(fileName), target);
    }

    /**
     * Read the JSON resource.
     *
     * @param resource the resource to open
     * @param clazz    the type of the final object
     * @param <T>      the type of the final object
     * @return the object
     */
    public static <T> T readFromResource(String resource, Class<T> clazz) {
        return readFromResource(resource, clazz, JsonTools.class);
    }

    /**
     * Read the JSON resource.
     *
     * @param resource the resource to open
     * @param clazz    the type of the final object
     * @param context  the context class to use relative path
     * @param <T>      the type of the final object
     * @return the object
     */
    public static <T> T readFromResource(String resource, Class<T> clazz, Class<?> context) {
        try {
            return PRETTY_OBJECT_MAPPER.readValue(context.getResourceAsStream(resource), clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Read the JSON resource.
     *
     * @param resource the resource to open
     * @param clazz    the type of the final object
     * @param <T>      the type of the final object
     * @return the list of objects
     */
    public static <T> List<T> readFromResourceAsList(String resource, Class<T> clazz) {
        return readFromResourceAsList(resource, clazz, JsonTools.class);
    }

    /**
     * Read the JSON resource.
     *
     * @param resource the resource to open
     * @param clazz    the type of the final object
     * @param context  the context class to use relative path
     * @param <T>      the type of the final object
     * @return the list of objects
     */
    public static <T> List<T> readFromResourceAsList(String resource, Class<T> clazz, Class<?> context) {
        try {
            CollectionType listType = PRETTY_OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
            return PRETTY_OBJECT_MAPPER.readValue(context.getResourceAsStream(resource), listType);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Read the JSON String.
     *
     * @param text  the json content
     * @param clazz the type of the final object
     * @param <T>   the type of the final object
     * @return the object
     */
    public static <T> T readFromString(String text, Class<T> clazz) {
        try {
            return PRETTY_OBJECT_MAPPER.readValue(text, clazz);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Read the JSON String.
     *
     * @param text  the json content
     * @param clazz the type of the final object
     * @param <T>   the type of the final object
     * @return the list of objects
     */
    public static <T> List<T> readFromStringAsList(String text, Class<T> clazz) {
        try {
            CollectionType listType = PRETTY_OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
            return PRETTY_OBJECT_MAPPER.readValue(text, listType);
        } catch (Exception e) {
            throw new SmallToolsException("Problem deserializing from JSON", e);
        }
    }

    /**
     * Write the JSON to the file.
     *
     * @param file   the file
     * @param object the object to serialize
     */
    public static void writeToFile(File file, Object object) {
        try {
            PRETTY_OBJECT_MAPPER.writeValue(file, object);
        } catch (IOException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    /**
     * Write the JSON to the file.
     *
     * @param fileName the full path to the file
     * @param object   the object to serialize
     */
    public static void writeToFile(String fileName, Object object) {
        writeToFile(new File(fileName), object);
    }

    /**
     * Write the JSON to the stream. The stream will not be closed.
     *
     * @param stream the stream
     * @param object the object to serialize
     */
    public static void writeToStream(OutputStream stream, Object object) {
        try {
            PRETTY_OBJECT_MAPPER.writeValue(stream, object);
        } catch (IOException e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    /**
     * Write the JSON to a String.
     *
     * @param object the object to serialize
     * @return the JSON string
     */
    public static String writeToString(Object object) {
        try {
            return PRETTY_OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new SmallToolsException("Problem serializing in JSON", e);
        }
    }

    private JsonTools() {
    }
}
