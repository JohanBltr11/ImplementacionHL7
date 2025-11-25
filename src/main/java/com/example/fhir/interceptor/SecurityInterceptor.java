package com.example.fhir.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import com.example.fhir.model.User;
import com.example.fhir.storage.UserStorage;
import com.example.fhir.util.AuthContext;
import org.apache.commons.codec.binary.Base64;

import java.util.Arrays;
import java.util.List;

/**
 * Interceptor de seguridad mejorado
 * Implementa autenticación HTTP Basic usando UserStorage
 * Control de acceso granular basado en roles y ownership
 * 
 * Roles disponibles:
 * - admin: Permiso completo (CRUD en todos los recursos)
 * - user: Solo puede consultar sus propios recursos
 */
@Interceptor
public class SecurityInterceptor {
    
    private final UserStorage userStorage = UserStorage.getInstance();
    
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLER_SELECTED)
    public void authenticate(RequestDetails requestDetails) {
        System.out.println("SecurityInterceptor: ===== INICIO authenticate (PRE_HANDLER_SELECTED) =====");
        System.out.println("SecurityInterceptor: Thread: " + Thread.currentThread().getName());
        System.out.println("SecurityInterceptor: RequestPath: " + (requestDetails != null ? requestDetails.getRequestPath() : "null"));
        
        // Si requestDetails es null, lanzar excepción
        if (requestDetails == null) {
            System.err.println("SecurityInterceptor: requestDetails es null!");
            throw new AuthenticationException("Error interno: requestDetails es null");
        }
        
        try {
            // Obtener header de autorización
            List<String> authHeaders = requestDetails.getHeaders("Authorization");
            
            // Log para depuración
            System.out.println("SecurityInterceptor: Headers recibidos - " + 
                (authHeaders != null ? authHeaders.size() + " headers" : "null"));
            if (authHeaders != null && !authHeaders.isEmpty()) {
                System.out.println("SecurityInterceptor: Primer header = " + 
                    (authHeaders.get(0) != null ? authHeaders.get(0).substring(0, Math.min(20, authHeaders.get(0).length())) + "..." : "null"));
            }
            
            if (authHeaders == null || authHeaders.isEmpty()) {
                System.err.println("SecurityInterceptor: No se encontraron headers de Authorization");
                throw new AuthenticationException("Se requiere autenticación. Use HTTP Basic Auth.");
            }
            
            String authHeader = authHeaders.get(0);
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                System.err.println("SecurityInterceptor: Header no es Basic Auth: " + 
                    (authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) : "null"));
                throw new AuthenticationException("Solo se soporta HTTP Basic Authentication");
            }
            
            // Decodificar credenciales
            String encoded = authHeader.substring(6);
            String decoded = new String(Base64.decodeBase64(encoded));
            String[] credentials = decoded.split(":", 2);
            
            if (credentials.length != 2) {
                System.err.println("SecurityInterceptor: Formato de credenciales inválido");
                throw new AuthenticationException("Formato de credenciales inválido");
            }
            
            String username = credentials[0];
            String password = credentials[1];
            
            System.out.println("SecurityInterceptor: Intentando autenticar usuario: " + username);
            
            // Validar credenciales usando UserStorage
            User user = userStorage.authenticate(username, password);
            if (user == null) {
                System.err.println("SecurityInterceptor: Credenciales inválidas para usuario: " + username);
                throw new AuthenticationException("Credenciales inválidas");
            }
            
            System.out.println("SecurityInterceptor: Usuario autenticado: " + username + " con rol: " + user.getRole());
            
            // Guardar información del usuario en request details y ThreadLocal
            try {
                // Guardar en RequestDetails (para compatibilidad)
                requestDetails.setAttribute("ROLE", user.getRole());
                requestDetails.setAttribute("USERNAME", user.getUsername());
                requestDetails.setAttribute("USER", user);
                if (user.getPatientId() != null) {
                    requestDetails.setAttribute("PATIENT_ID", user.getPatientId());
                }
                
                // Guardar en ThreadLocal (más confiable)
                AuthContext.setUser(user);
                
                // Verificar inmediatamente que se guardó
                User verifyUser = AuthContext.getUser();
                System.out.println("SecurityInterceptor: Usuario guardado en RequestDetails y ThreadLocal");
                System.out.println("SecurityInterceptor: Verificación ThreadLocal - USER=" + 
                    (verifyUser != null ? verifyUser.getUsername() + " (rol: " + verifyUser.getRole() + ")" : "NULL"));
            } catch (Exception e) {
                System.err.println("SecurityInterceptor: Error al guardar atributos: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Verificar permisos según operación
            RestOperationTypeEnum operationType = requestDetails.getRestOperationType();
            if (operationType != null) {
                System.out.println("SecurityInterceptor: Verificando permisos para operación: " + operationType);
                checkPermissions(user, operationType);
            }
        } catch (AuthenticationException | ForbiddenOperationException e) {
            // Re-lanzar excepciones de autenticación/autorización
            System.err.println("SecurityInterceptor: Error de autenticación/autorización: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            // Cualquier otro error se convierte en error de autenticación
            System.err.println("SecurityInterceptor: Error inesperado: " + e.getMessage());
            e.printStackTrace();
            throw new AuthenticationException("Error durante la autenticación: " + e.getMessage(), e);
        }
    }
    
    private void checkPermissions(User user, RestOperationTypeEnum operationType) {
        // Admin tiene todos los permisos
        if (user.isAdmin()) {
            return;
        }
        
        // Usuarios regulares solo pueden leer (READ y SEARCH)
        // El filtrado por ownership se hace en los providers
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
    
    /**
     * Limpiar el ThreadLocal después de procesar la petición
     */
    @Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
    public void cleanup(RequestDetails requestDetails) {
        AuthContext.clear();
    }
}

