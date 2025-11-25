package com.example.fhir.storage;

import com.example.fhir.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Almacenamiento de usuarios en archivo JSON
 * Thread-safe usando ConcurrentHashMap
 */
public class UserStorage {
    
    private static final UserStorage instance = new UserStorage();
    private static final String USERS_FILE = "users.json";
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private UserStorage() {
        loadUsers();
        initializeDefaultUsers();
    }
    
    public static UserStorage getInstance() {
        return instance;
    }
    
    /**
     * Cargar usuarios desde archivo JSON
     */
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (file.exists()) {
            try {
                // Configurar ObjectMapper para ignorar campos desconocidos
                objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                List<User> userList = objectMapper.readValue(file, new TypeReference<List<User>>() {});
                userList.forEach(user -> users.put(user.getUsername(), user));
            } catch (IOException e) {
                System.err.println("Error al cargar usuarios: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Guardar usuarios en archivo JSON
     */
    private void saveUsers() {
        try {
            List<User> userList = new ArrayList<>(users.values());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(USERS_FILE), userList);
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Inicializar usuarios por defecto si no existen
     */
    private void initializeDefaultUsers() {
        if (users.isEmpty()) {
            // Usuario administrador por defecto
            User admin = new User("admin", "admin123", "admin", null, "admin@example.com");
            users.put("admin", admin);
            
            // Usuario regular por defecto
            User user = new User("user", "user123", "user", null, "user@example.com");
            users.put("user", user);
            
            saveUsers();
        }
    }
    
    /**
     * Registrar un nuevo usuario
     */
    public boolean registerUser(String username, String password, String email, String patientId) {
        if (users.containsKey(username)) {
            return false; // Usuario ya existe
        }
        
        User newUser = new User(username, password, "user", patientId, email);
        users.put(username, newUser);
        saveUsers();
        return true;
    }
    
    /**
     * Autenticar un usuario
     */
    public User authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    
    /**
     * Obtener un usuario por username
     */
    public User getUser(String username) {
        return users.get(username);
    }
    
    /**
     * Verificar si un usuario existe
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
    
    /**
     * Obtener todos los usuarios (solo para administradores)
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    /**
     * Actualizar información de un usuario
     */
    public void updateUser(User user) {
        users.put(user.getUsername(), user);
        saveUsers();
    }
    
    /**
     * Eliminar un usuario
     */
    public boolean deleteUser(String username) {
        if (users.remove(username) != null) {
            saveUsers();
            return true;
        }
        return false;
    }
    
    /**
     * Asociar un Patient ID a un usuario
     */
    public void associatePatientId(String username, String patientId) {
        User user = users.get(username);
        if (user != null) {
            user.setPatientId(patientId);
            saveUsers();
        }
    }
}

