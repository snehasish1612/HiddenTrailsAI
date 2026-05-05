package com.hiddentrails.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JWTUtil — generates and validates JWT tokens using JJWT 0.11.x.
 *
 * Token payload stores:  sub (userId), role
 * Expiry loaded from:    app.properties → jwt.expiry.hours  (default 24)
 * Secret loaded from:    app.properties → jwt.secret
 */
public class JWTUtil {

    private static final Logger LOGGER = Logger.getLogger(JWTUtil.class.getName());
    private static final Key    SECRET_KEY;
    private static final long   EXPIRY_MS;

    static {
        try (InputStream in = JWTUtil.class
                .getClassLoader()
                .getResourceAsStream("app.properties")) {

            Properties props = new Properties();
            if (in != null) props.load(in);

            String secret = props.getProperty(
                "jwt.secret", "HiddenTrailsAI_DefaultSecret_ChangeInProd!");
            int hours = Integer.parseInt(
                props.getProperty("jwt.expiry.hours", "24"));

            SECRET_KEY = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
            EXPIRY_MS  = (long) hours * 60 * 60 * 1000;

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load app.properties for JWT", e);
            throw new RuntimeException("Cannot initialise JWTUtil", e);
        }
    }

    private JWTUtil() {}

    /**
     * Generates a signed JWT for the given user.
     *
     * @param userId  the user's primary key
     * @param role    "user" or "admin"
     * @return signed JWT string
     */
    public static String generateToken(int userId, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + EXPIRY_MS);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates a token and returns its Claims.
     * Throws JwtException (caught by AuthFilter) if invalid or expired.
     */
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts userId (subject) from a token without re-validating.
     * Only call after validateToken() has already succeeded.
     */
    public static int extractUserId(String token) {
        Claims claims = validateToken(token);
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Extracts the role claim from a token.
     */
    public static String extractRole(String token) {
        Claims claims = validateToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Strips "Bearer " prefix from the Authorization header value.
     * Returns null if the header is missing or malformed.
     */
    public static String extractBearerToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }
}