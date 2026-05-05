package com.hiddentrails.model;

import java.sql.Timestamp;

public class User {
    private int       userId;
    private String    name;
    private String    email;
    private String    phone;
    private String    passwordHash;
    private String    role;          // "user" | "admin"
    private boolean   isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {}

    // ── Getters ────────────────────────────────────────────────
    public int       getUserId()      { return userId; }
    public String    getName()        { return name; }
    public String    getEmail()       { return email; }
    public String    getPhone()       { return phone; }
    public String    getPasswordHash(){ return passwordHash; }
    public String    getRole()        { return role; }
    public boolean   isActive()       { return isActive; }
    public Timestamp getCreatedAt()   { return createdAt; }
    public Timestamp getUpdatedAt()   { return updatedAt; }

    // ── Setters ────────────────────────────────────────────────
    public void setUserId(int userId)           { this.userId = userId; }
    public void setName(String name)            { this.name = name; }
    public void setEmail(String email)          { this.email = email; }
    public void setPhone(String phone)          { this.phone = phone; }
    public void setPasswordHash(String h)       { this.passwordHash = h; }
    public void setRole(String role)            { this.role = role; }
    public void setActive(boolean active)       { this.isActive = active; }
    public void setCreatedAt(Timestamp t)       { this.createdAt = t; }
    public void setUpdatedAt(Timestamp t)       { this.updatedAt = t; }
}