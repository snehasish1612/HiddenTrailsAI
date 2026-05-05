package com.hiddentrails.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hiddentrails.exception.AIServiceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends itinerary prompts to Gemini or OpenAI-compatible chat APIs.
 *
 * Config read from app.properties:
 *   ai.provider        = gemini | openai
 *   ai.api.key         = API key
 *   ai.api.url         = provider endpoint
 *   ai.model           = model name
 *   ai.timeout.seconds = request timeout
 */
public class AIService {

    private static final Logger LOGGER = Logger.getLogger(AIService.class.getName());

    private final String provider;
    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final int timeoutMs;

    public AIService() {
        try (InputStream in = AIService.class
                .getClassLoader()
                .getResourceAsStream("app.properties")) {

            Properties props = new Properties();
            if (in != null) {
                props.load(in);
            }

            this.provider = props.getProperty("ai.provider", "openai").trim().toLowerCase();
            this.apiKey = props.getProperty("ai.api.key", "").trim();
            this.apiUrl = props.getProperty(
                    "ai.api.url",
                    "https://api.openai.com/v1/chat/completions").trim();
            this.model = props.getProperty("ai.model", "gpt-4o").trim();
            this.timeoutMs = Integer.parseInt(
                    props.getProperty("ai.timeout.seconds", "30")) * 1000;

        } catch (IOException e) {
            throw new AIServiceException("Cannot load app.properties for AIService", e);
        }
    }

    public String generateItinerary(String prompt) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("your-gemini-api-key")) {
            throw new AIServiceException(
                    "AI API key is not configured in app.properties (ai.api.key)");
        }

        String requestBody = buildRequestBody(prompt);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(buildRequestUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            if (!isGemini()) {
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            }

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            InputStream stream = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody = readStream(stream);

            if (status < 200 || status >= 300) {
                LOGGER.warning("AI API returned HTTP " + status + ": " + responseBody);
                throw new AIServiceException(
                        "AI API error (HTTP " + status + "). Check your API key and quota.");
            }

            return extractContent(responseBody);

        } catch (SocketTimeoutException e) {
            throw new AIServiceException(
                    "AI API timed out after " + (timeoutMs / 1000) + " seconds.", e);
        } catch (IOException e) {
            throw new AIServiceException("Network error calling AI API: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private boolean isGemini() {
        return "gemini".equals(provider) || apiUrl.contains("generativelanguage.googleapis.com");
    }

    private String buildRequestUrl() {
        if (!isGemini()) {
            return apiUrl;
        }

        String separator = apiUrl.contains("?") ? "&" : "?";
        return apiUrl + separator + "key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
    }

    private String buildRequestBody(String prompt) {
        if (isGemini()) {
            return buildGeminiRequestBody(prompt);
        }
        return buildOpenAiCompatibleRequestBody(prompt);
    }

    private String buildOpenAiCompatibleRequestBody(String prompt) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject body = new JsonObject();
        body.addProperty("model", model);
        body.add("messages", messages);
        body.addProperty("max_tokens", 3000);
        body.addProperty("temperature", 0.7);

        return new Gson().toJson(body);
    }

    private String buildGeminiRequestBody(String prompt) {
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 8192);
        generationConfig.addProperty("candidateCount", 1);
        generationConfig.addProperty("responseMimeType", "application/json");

        JsonObject body = new JsonObject();
        body.add("contents", contents);
        body.add("generationConfig", generationConfig);

        return new Gson().toJson(body);
    }

    private String extractContent(String responseBody) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            String content = isGemini()
                    ? extractGeminiContent(root)
                    : extractOpenAiCompatibleContent(root);

            content = cleanJsonContent(content);
            content = sanitizeJsonStringNewlines(content);

            if (!content.startsWith("{")) {
                LOGGER.warning("AI response does not start with {: " + content);
                throw new AIServiceException(
                        "AI returned unexpected format. Please try again.");
            }

            validateJson(content);
            return content;

        } catch (JsonSyntaxException | NullPointerException e) {
            LOGGER.log(Level.WARNING, "Failed to parse AI response", e);
            throw new AIServiceException("Failed to parse AI API response.", e);
        }
    }

    private String extractOpenAiCompatibleContent(JsonObject root) {
        JsonArray choices = root.getAsJsonArray("choices");

        if (choices == null || choices.size() == 0) {
            throw new AIServiceException("AI API returned no choices in response.");
        }

        return choices.get(0)
                .getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString()
                .trim();
    }

    private String extractGeminiContent(JsonObject root) {
        JsonArray candidates = root.getAsJsonArray("candidates");

        if (candidates == null || candidates.size() == 0) {
            throw new AIServiceException("Gemini returned no candidates in response.");
        }

        JsonObject candidate = candidates.get(0).getAsJsonObject();
        JsonObject content = candidate.getAsJsonObject("content");
        JsonArray parts = content == null ? null : content.getAsJsonArray("parts");

        if (parts == null || parts.size() == 0) {
            throw new AIServiceException("Gemini returned no text parts in response.");
        }

        return parts.get(0)
                .getAsJsonObject()
                .get("text")
                .getAsString()
                .trim();
    }

    private String cleanJsonContent(String content) {
        if (content.startsWith("```")) {
            content = content
                    .replaceAll("^```[a-zA-Z]*\\n?", "")
                    .replaceAll("```$", "")
                    .trim();
        }
        return content;
    }

    private String sanitizeJsonStringNewlines(String content) {
        StringBuilder out = new StringBuilder(content.length());
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (escaped) {
                out.append(ch);
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                out.append(ch);
                escaped = true;
                continue;
            }

            if (ch == '"') {
                inString = !inString;
                out.append(ch);
                continue;
            }

            if (inString && (ch == '\n' || ch == '\r')) {
                out.append(' ');
                continue;
            }

            out.append(ch);
        }

        return out.toString();
    }

    private void validateJson(String content) {
        JsonParser.parseString(content).getAsJsonObject();
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString().trim();
        }
    }
}
