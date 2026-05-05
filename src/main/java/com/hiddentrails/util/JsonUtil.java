package com.hiddentrails.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

/**
 * JsonUtil — shared Gson wrapper used across DAOs and Services.
 *
 * Keeps a single configured Gson instance so date formats,
 * null serialisation, and pretty-printing are consistent
 * across the entire application.
 *
 * Usage examples:
 *   String json   = JsonUtil.toJson(myObject);
 *   MyClass obj   = JsonUtil.fromJson(json, MyClass.class);
 *   List<String>  = JsonUtil.fromJsonList(json, String.class);
 */
public class JsonUtil {

    // ── Standard instance — used for API responses ─────────────
    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    // ── Pretty-print instance — used for logging/debug ─────────
    private static final Gson GSON_PRETTY = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private JsonUtil() {}

    // ── Serialisation ───────────────────────────────────────────

    /**
     * Serialises any object to a compact JSON string.
     */
    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    /**
     * Serialises any object to a pretty-printed JSON string.
     * Used for debug logging only.
     */
    public static String toPrettyJson(Object obj) {
        return GSON_PRETTY.toJson(obj);
    }

    // ── Deserialisation ─────────────────────────────────────────

    /**
     * Deserialises a JSON string to the given class type.
     *
     * @param json      the JSON string
     * @param classOfT  the target class, e.g. User.class
     * @return          a populated instance of classOfT
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    /**
     * Deserialises a JSON array string to a List of the given type.
     *
     * Example:
     *   List<String> styles = JsonUtil.fromJsonList(
     *       "[\"adventure\",\"nature\"]", String.class);
     *
     * @param json      the JSON array string
     * @param classOfT  the element type
     * @return          a List of classOfT
     */
    public static <T> List<T> fromJsonList(String json, Class<T> classOfT) {
        Type listType = TypeToken.getParameterized(List.class, classOfT).getType();
        return GSON.fromJson(json, listType);
    }

    /**
     * Parses a JSON string to a JsonObject for manual field extraction.
     * Useful in servlet body parsers.
     */
    public static JsonObject parseObject(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    /**
     * Parses a JSON string to a JsonArray.
     */
    public static JsonArray parseArray(String json) {
        return JsonParser.parseString(json).getAsJsonArray();
    }

    /**
     * Returns true if the string is valid JSON.
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isBlank()) return false;
        try {
            JsonParser.parseString(json);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    /**
     * Safely reads a String field from a JsonObject.
     * Returns defaultValue if the field is absent or null.
     */
    public static String getString(JsonObject obj, String key, String defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull())
            return defaultValue;
        return obj.get(key).getAsString();
    }

    /**
     * Safely reads an int field from a JsonObject.
     * Returns defaultValue if the field is absent or null.
     */
    public static int getInt(JsonObject obj, String key, int defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull())
            return defaultValue;
        return obj.get(key).getAsInt();
    }

    /**
     * Safely reads a boolean field from a JsonObject.
     */
    public static boolean getBoolean(JsonObject obj, String key, boolean defaultValue) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull())
            return defaultValue;
        return obj.get(key).getAsBoolean();
    }

    /**
     * Exposes the raw Gson instance for cases where TypeAdapter
     * or custom serialiser registration is needed.
     */
    public static Gson getInstance() {
        return GSON;
    }
}