package com.fourati.platform.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Map;
import java.util.Optional;

/**
 * JSON utilities wrapping Jackson.
 * Uses a preconfigured ObjectMapper (Java 8 time, no null fields, fail on unknown: off).
 *
 * For complex use-cases inject the Spring-managed ObjectMapper bean instead.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtils() {}

    /** Serializes an object to a compact JSON string. Returns null on error. */
    public static String toJson(Object value) {
		if (value == null)
			return null;
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize to JSON: " + e.getMessage(), e);
        }
    }

    /** Serializes an object to a pretty-printed JSON string. */
    public static String toPrettyJson(Object value) {
		if (value == null)
			return null;
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize to JSON: " + e.getMessage(), e);
        }
    }

    /** Deserializes a JSON string to the given class. */
    public static <T> T fromJson(String json, Class<T> type) {
		if (json == null || json.isBlank())
			return null;
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot deserialize JSON: " + e.getMessage(), e);
        }
    }

    /** Deserializes using a TypeReference — useful for generics like List<MyDto>. */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
		if (json == null || json.isBlank())
			return null;
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot deserialize JSON: " + e.getMessage(), e);
        }
    }

    /** Deserializes safely — returns empty Optional on error. */
    public static <T> Optional<T> tryFromJson(String json, Class<T> type) {
        try {
            return Optional.ofNullable(fromJson(json, type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Converts any object to a Map<String, Object> (shallow). */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object value) {
        return MAPPER.convertValue(value, Map.class);
    }

    /** Deep-copies an object by serializing and deserializing it. */
    public static <T> T deepCopy(T value, Class<T> type) {
        return fromJson(toJson(value), type);
    }
}
