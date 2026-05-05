package com.hiddentrails.servlet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hiddentrails.exception.AuthException;
import com.hiddentrails.exception.DAOException;
import com.hiddentrails.service.AuthService;
import com.hiddentrails.util.ResponseUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * AuthServlet — handles user login and registration.
 *
 * POST /api/auth/login    → { email, password }
 * POST /api/auth/register → { name, email, phone, password }
 *
 * Both return: { token, userId, name, email, role }
 * Bypassed by AuthFilter (public endpoints).
 */
@WebServlet(urlPatterns = {"/api/auth/login", "/api/auth/register"})
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String path = req.getRequestURI();
        JsonObject body = parseBody(req);

        try {
            if (path.endsWith("/login")) {
                handleLogin(body, resp);
            } else if (path.endsWith("/register")) {
                handleRegister(body, resp);
            } else {
                ResponseUtil.sendError(resp, 404, "NOT_FOUND", "Endpoint not found");
            }
        } catch (AuthException e) {
            ResponseUtil.sendError(resp, 401, "AUTH_ERROR", e.getMessage());
        } catch (DAOException e) {
            ResponseUtil.sendError(resp, 400, "VALIDATION_ERROR", e.getMessage());
        } catch (Exception e) {
            ResponseUtil.sendError(resp, 500, "SERVER_ERROR",
                "An unexpected error occurred");
        }
    }

    // ── Login ─────────────────────────────────────────────────────
    private void handleLogin(JsonObject body, HttpServletResponse resp)
            throws IOException {

        String email    = getRequired(body, "email");
        String password = getRequired(body, "password");

        if (email == null || password == null) {
            ResponseUtil.sendError(resp, 400, "VALIDATION_ERROR",
                "email and password are required");
            return;
        }

        Map<String, Object> result = authService.login(email, password);
        ResponseUtil.sendJson(resp, result);
    }

    // ── Register ──────────────────────────────────────────────────
    private void handleRegister(JsonObject body, HttpServletResponse resp)
            throws IOException {

        String name     = getRequired(body, "name");
        String email    = getRequired(body, "email");
        String phone    = getString  (body, "phone");
        String password = getRequired(body, "password");

        if (name == null || email == null || password == null) {
            ResponseUtil.sendError(resp, 400, "VALIDATION_ERROR",
                "name, email and password are required");
            return;
        }
        if (password.length() < 6) {
            ResponseUtil.sendError(resp, 400, "VALIDATION_ERROR",
                "password must be at least 6 characters");
            return;
        }

        int userId = authService.register(name, email, phone, password);

        // Auto-login after registration
        Map<String, Object> result = authService.login(email, password);
        result.put("message", "Registration successful");
        ResponseUtil.sendJson(resp, result);
    }

    // ── Helpers ───────────────────────────────────────────────────
    private JsonObject parseBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        try {
            return JsonParser.parseString(sb.toString()).getAsJsonObject();
        } catch (Exception e) {
            return new JsonObject();
        }
    }

    private String getRequired(JsonObject body, String key) {
        if (!body.has(key) || body.get(key).isJsonNull()) return null;
        String val = body.get(key).getAsString().trim();
        return val.isEmpty() ? null : val;
    }

    private String getString(JsonObject body, String key) {
        if (!body.has(key) || body.get(key).isJsonNull()) return null;
        return body.get(key).getAsString().trim();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCors(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}