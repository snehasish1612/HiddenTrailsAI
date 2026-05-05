package com.hiddentrails.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hiddentrails.dao.UserDAO;
import com.hiddentrails.exception.AuthException;
import com.hiddentrails.model.User;
import com.hiddentrails.util.JWTUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Registers a new user.
     * Returns the generated userId.
     */
    public int register(String name, String email,
                        String phone, String rawPassword) {

        if (userDAO.emailExists(email)) {
            throw new AuthException("An account with this email already exists.");
        }

        String hash = BCrypt.withDefaults()
                            .hashToString(12, rawPassword.toCharArray());

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(hash);
        user.setRole("user");

        return userDAO.save(user);
    }

    /**
     * Authenticates a user and returns a JWT token map.
     * Returns: { token, userId, name, email, role }
     */
    public Map<String, Object> login(String email, String rawPassword) {

        User user = userDAO.findByEmail(email);

        if (user == null) {
            throw new AuthException("Invalid email or password.");
        }

        BCrypt.Result result = BCrypt.verifyer()
                .verify(rawPassword.toCharArray(), user.getPasswordHash());

        if (!result.verified) {
            throw new AuthException("Invalid email or password.");
        }

        String token = JWTUtil.generateToken(user.getUserId(), user.getRole());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token",  token);
        response.put("userId", user.getUserId());
        response.put("name",   user.getName());
        response.put("email",  user.getEmail());
        response.put("role",   user.getRole());
        return response;
    }
}