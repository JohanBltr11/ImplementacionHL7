package com.example.fhir.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.List;

/**
 * Interceptor de seguridad básica
 * Implementa autenticación HTTP Basic y control de roles
 * 
 * Roles disponibles:
 * - admin: Permiso completo (CRUD)
 * - user: Solo lectura (GET)
 * 
 * Ejemplo de uso:
 * - Usuario admin: admin:admin123
 * - Usuario user: user:user123
 */
@Interceptor
public class SecurityInterceptor {
    
    // Credenciales hardcodeadas para demostración (en producción usar base de datos)
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASS = "admin123";
    private static final String USER_USER = "user";
    private static final String USER_PASS = "user123";
    
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
    public void authenticate(RequestDetails requestDetails) {
        // Si requestDetails es null, simplemente retornar (puede pasar en algunos contextos)
        if (requestDetails == null) {
            // Log para depuración
            System.err.println("SecurityInterceptor: requestDetails es null, retornando sin autenticar");
            return;
        }
        
        try {
            // Obtener header de autorización
            List<String> authHeaders = requestDetails.getHeaders("Authorization");
            
            // Log para depuración
            System.out.println("SecurityInterceptor: Headers recibidos - " + 
                (authHeaders != null ? authHeaders.size() + " headers" : "null"));
            
            if (authHeaders == null || authHeaders.isEmpty()) {
                throw new AuthenticationException("Se requiere autenticación. Use HTTP Basic Auth.");
            }
            
            String authHeader = authHeaders.get(0);
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                throw new AuthenticationException("Solo se soporta HTTP Basic Authentication");
            }
            
            // Decodificar credenciales
            String encoded = authHeader.substring(6);
            String decoded = new String(Base64.decodeBase64(encoded));
            String[] credentials = decoded.split(":", 2);
            
            if (credentials.length != 2) {
                throw new AuthenticationException("Formato de credenciales inválido");
            }
            
            String username = credentials[0];
            String password = credentials[1];
            
            // Validar credenciales y asignar rol
            String role = validateCredentials(username, password);
            if (role == null) {
                throw new AuthenticationException("Credenciales inválidas");
            }
            
            // Guardar rol en request details para uso posterior
            try {
                requestDetails.setAttribute("ROLE", role);
                requestDetails.setAttribute("USERNAME", username);
            } catch (Exception e) {
                // Si no se puede guardar, continuar de todas formas
                // El error se manejará en la verificación de permisos
            }
            
            // Verificar permisos según operación
            RestOperationTypeEnum operationType = requestDetails.getRestOperationType();
            if (operationType != null) {
                checkPermissions(role, operationType);
            }
        } catch (AuthenticationException | ForbiddenOperationException e) {
            // Re-lanzar excepciones de autenticación/autorización
            throw e;
        } catch (Exception e) {
            // Cualquier otro error se convierte en error de autenticación
            throw new AuthenticationException("Error durante la autenticación: " + e.getMessage(), e);
        }
    }
    
    private String validateCredentials(String username, String password) {
        if (ADMIN_USER.equals(username) && ADMIN_PASS.equals(password)) {
            return "admin";
        }
        if (USER_USER.equals(username) && USER_PASS.equals(password)) {
            return "user";
        }
        return null;
    }
    
    private void checkPermissions(String role, RestOperationTypeEnum operationType) {
        // Admin tiene todos los permisos
        if ("admin".equals(role)) {
            return;
        }
        
        // User solo puede leer
        if ("user".equals(role)) {
            List<RestOperationTypeEnum> allowedOps = Arrays.asList(
                RestOperationTypeEnum.READ,
                RestOperationTypeEnum.SEARCH_TYPE,
                RestOperationTypeEnum.VREAD,
                RestOperationTypeEnum.HISTORY_TYPE
            );
            
            if (!allowedOps.contains(operationType)) {
                throw new ForbiddenOperationException(
                    "El rol 'user' solo tiene permisos de lectura. Operación '" + 
                    operationType + "' no permitida."
                );
            }
        }
    }
}

