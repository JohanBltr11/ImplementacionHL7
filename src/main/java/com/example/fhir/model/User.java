package com.example.fhir.model;

import java.io.Serializable;

/**
 * Modelo de Usuario para el sistema de autenticación
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private String role; // "admin" o "user"
    private String patientId; // ID del Patient asociado al usuario (solo para usuarios regulares)
    private String email;
    
    public User() {
    }
    
    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    public User(String username, String password, String role, String patientId, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.patientId = patientId;
        this.email = email;
    }
    
    // Getters y Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isAdmin() {
        return "admin".equals(role);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", patientId='" + patientId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

