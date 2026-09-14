package com.fourati.platform.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Optional;

/**
 * JSON utilities on Jackson 3 (Boot 4's JSON stack; {@code java.time} support is built in).
 * Uses a preconfigured mapper: no null fields, unknown properties ignored (ISO dates are the Jackson 3 default).
 *
 * For complex use-cases inject the Spring-managed {@code JsonMapper} bean instead.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
        .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    private JsonUtils() {}

    /** Serializes an object to a compact JSON string. Returns null for null. */
    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot serialize to JSON: " + e.getMessage(), e);
        }
    }

    /** Serializes an object to a pretty-printed JSON string. */
    public static String toPrettyJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot serialize to JSON: " + e.getMessage(), e);
        }
    }

    /** Deserializes a JSON string to the given class. */
    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, type);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot deserialize JSON: " + e.getMessage(), e);
        }
    }

    /** Deserializes using a TypeReference, for generics like {@code List<MyDto>}. */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Cannot deserialize JSON: " + e.getMessage(), e);
        }
    }

    /** Deserializes safely: empty Optional on error. */
    public static <T> Optional<T> tryFromJson(String json, Class<T> type) {
        try {
            return Optional.ofNullable(fromJson(json, type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Converts any object to a {@code Map<String, Object>} (shallow). */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object value) {
        return MAPPER.convertValue(value, Map.class);
    }

    /** Deep-copies an object by serializing and deserializing it. */
    public static <T> T deepCopy(T value, Class<T> type) {
        return fromJson(toJson(value), type);
    }
}
