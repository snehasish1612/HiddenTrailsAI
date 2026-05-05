package com.hiddentrails.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ResponseUtil — writes consistent JSON responses from every servlet.
 *
 * All servlets use sendJson() or sendError() instead of writing
 * directly to response.getWriter(), ensuring Content-Type and
 * status codes are always set correctly.
 */
public class ResponseUtil {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .serializeNulls()
            .create();

    private ResponseUtil() {}

    /**
     * Serialises any object to JSON and writes it to the response.
     * Sets Content-Type: application/json and HTTP 200.
     */
    public static void sendJson(HttpServletResponse response, Object data)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.print(GSON.toJson(data));
        out.flush();
    }

    /**
     * Sends a JSON error envelope: { "error": "CODE", "message": "...", "code": 4xx }
     */
    public static void sendError(HttpServletResponse response,
                                  int statusCode,
                                  String errorCode,
                                  String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(statusCode);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error",   errorCode);
        error.put("message", message);
        error.put("code",    statusCode);

        PrintWriter out = response.getWriter();
        out.print(GSON.toJson(error));
        out.flush();
    }

    /**
     * Convenience — sends a plain success message.
     */
    public static void sendSuccess(HttpServletResponse response, String message)
            throws IOException {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        sendJson(response, body);
    }

    /**
     * Exposes the shared Gson instance so DAOs/services can reuse it.
     */
    public static Gson getGson() {
        return GSON;
    }
}