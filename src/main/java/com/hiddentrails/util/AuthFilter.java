package com.hiddentrails.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AuthFilter — protects every /api/* endpoint except /api/auth/*.
 *
 * On each request:
 *  1. Reads the Authorization header.
 *  2. Strips the "Bearer " prefix and validates the JWT.
 *  3. Attaches userId and role as request attributes for servlets to read.
 *  4. Returns HTTP 401 if the token is missing, expired, or invalid.
 *
 * Mapped to /api/* in web.xml (or via @WebFilter below).
 */

public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // Nothing to initialise
    }

    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse res,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Allow CORS preflight through without auth
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        String path = request.getRequestURI();

        // Public endpoints — skip JWT check
        if (path.contains("/api/auth/")
                || path.contains("/api/destinations")) {
            chain.doFilter(req, res);
            return;
        }

        // Extract token from header
        String authHeader = request.getHeader("Authorization");
        String token      = JWTUtil.extractBearerToken(authHeader);

        if (token == null) {
            ResponseUtil.sendError(response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "MISSING_TOKEN",
                "Authorization header is required");
            return;
        }

        try {
            Claims claims = JWTUtil.validateToken(token);

            // Attach userId and role as request attributes
            int    userId = Integer.parseInt(claims.getSubject());
            String role   = claims.get("role", String.class);

            request.setAttribute("userId", userId);
            request.setAttribute("role",   role);

            chain.doFilter(req, res);

        } catch (JwtException | IllegalArgumentException e) {
            ResponseUtil.sendError(response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "INVALID_TOKEN",
                "Token is invalid or has expired. Please log in again.");
        }
    }

    @Override
    public void destroy() {
        // Nothing to clean up
    }
}