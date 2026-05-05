package com.hiddentrails.service;

import com.google.gson.*;
import com.hiddentrails.exception.AIServiceException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AIService — sends the itinerary prompt to OpenAI (or Gemini)
 * and returns the raw JSON string from the model.
 *
 * Config read from app.properties:
 *   ai.api.key        = sk-...
 *   ai.api.url        = https://api.openai.com/v1/chat/completions
 *   ai.model          = gpt-4o
 *   ai.timeout.seconds= 30
 */
public class AIService {

    private static final Logger LOGGER = Logger.getLogger(AIService.class.getName());

    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final int    timeoutMs;

    public AIService() {
        try (InputStream in = AIService.class
                .getClassLoader()
                .getResourceAsStream("app.properties")) {

            Properties props = new Properties();
            if (in != null) props.load(in);

            this.apiKey    = props.getProperty("ai.api.key",    "");
            this.apiUrl    = props.getProperty("ai.api.url",
                             "https://api.openai.com/v1/chat/completions");
            this.model     = props.getProperty("ai.model",      "gpt-4o");
            this.timeoutMs = Integer.parseInt(
                             props.getProperty("ai.timeout.seconds", "30")) * 1000;

        } catch (IOException e) {
            throw new AIServiceException("Cannot load app.properties for AIService", e);
        }
    }

    /**
     * Sends the prompt to the AI API.
     *
     * @param prompt  the fully assembled prompt from PromptBuilder
     * @return        raw JSON string returned by the model
     * @throws AIServiceException on timeout, HTTP error, or bad response
     */
    public String generateItinerary(String prompt) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new AIServiceException(
                "AI API key is not configured in app.properties (ai.api.key)");
        }

        String requestBody = buildRequestBody(prompt);

        HttpURLConnection conn = null;
        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type",  "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            // Write request body
            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();

            // Read response (error stream on 4xx/5xx)
            InputStream stream = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody = readStream(stream);

            if (status != 200) {
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
            if (conn != null) conn.disconnect();
        }
    }

    // ── Build OpenAI-compatible JSON request body ─────────────────
    private String buildRequestBody(String prompt) {
        JsonObject message = new JsonObject();
        message.addProperty("role",    "user");
        message.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(message);

        JsonObject body = new JsonObject();
        body.addProperty("model",       model);
        body.add        ("messages",    messages);
        body.addProperty("max_tokens",  2000);
        body.addProperty("temperature", 0.7);

        return new Gson().toJson(body);
    }

    // ── Extract the text content from OpenAI response ─────────────
    private String extractContent(String responseBody) {
        try {
            JsonObject root    = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray  choices = root.getAsJsonArray("choices");

            if (choices == null || choices.size() == 0) {
                throw new AIServiceException(
                    "AI API returned no choices in response.");
            }

            String content = choices.get(0)
                    .getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content")
                    .getAsString()
                    .trim();

            // Strip markdown code fences if present (```json ... ```)
            if (content.startsWith("```")) {
                content = content
                    .replaceAll("^```[a-zA-Z]*\\n?", "")
                    .replaceAll("```$", "")
                    .trim();
            }

            // Validate it looks like JSON
            if (!content.startsWith("{")) {
                LOGGER.warning("AI response does not start with {: " + content);
                throw new AIServiceException(
                    "AI returned unexpected format. Please try again.");
            }

            return content;

        } catch (JsonSyntaxException | NullPointerException e) {
            LOGGER.log(Level.WARNING, "Failed to parse AI response", e);
            throw new AIServiceException("Failed to parse AI API response.", e);
        }
    }

    // ── Read an InputStream fully into a String ───────────────────
    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
            return sb.toString().trim();
        }
    }
}