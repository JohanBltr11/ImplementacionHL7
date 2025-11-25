package com.example.fhir.util;

import com.example.fhir.model.User;

/**
 * Contexto de autenticación usando ThreadLocal
 * Permite almacenar el usuario autenticado durante el procesamiento de una petición
 */
public class AuthContext {
    
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();
    
    /**
     * Establecer el usuario autenticado para el hilo actual
     */
    public static void setUser(User user) {
        currentUser.set(user);
    }
    
    /**
     * Obtener el usuario autenticado del hilo actual
     */
    public static User getUser() {
        return currentUser.get();
    }
    
    /**
     * Limpiar el usuario del hilo actual (importante para evitar memory leaks)
     */
    public static void clear() {
        currentUser.remove();
    }
}

