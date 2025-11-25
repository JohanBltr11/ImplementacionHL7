package com.example.fhir.interceptor;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interceptor de trazabilidad
 * Registra todas las operaciones en un archivo de log
 */
@Interceptor
public class LoggingInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    private static final String LOG_FILE = "fhir-server.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_PROCESSED)
    public void logRequest(RequestDetails requestDetails) {
        if (requestDetails == null) {
            logger.warn("RequestDetails es null en logRequest");
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String method = requestDetails.getRequestType() != null ? requestDetails.getRequestType().name() : "UNKNOWN";
            String path = requestDetails.getRequestPath();
            RestOperationTypeEnum operationType = requestDetails.getRestOperationType();
            
            // Verificar headers de Authorization
            java.util.List<String> authHeaders = requestDetails.getHeaders("Authorization");
            String authHeaderInfo = authHeaders != null && !authHeaders.isEmpty() ? 
                "PRESENT (" + authHeaders.size() + ")" : "MISSING";
            
            String username = (String) requestDetails.getAttribute("USERNAME");
            String role = (String) requestDetails.getAttribute("ROLE");
            
            System.out.println("LoggingInterceptor: Request recibido - Method=" + method + 
                ", Path=" + path + ", AuthHeader=" + authHeaderInfo);
            
            String logEntry = String.format(
                "[%s] OPERACION=%s | METODO=%s | PATH=%s | USUARIO=%s | ROL=%s",
                timestamp,
                operationType != null ? operationType.name() : "UNKNOWN",
                method,
                path,
                username != null ? username : "ANONYMOUS",
                role != null ? role : "NONE"
            );
            
            // Escribir en archivo
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                writer.println(logEntry);
            }
            
            // También en consola
            logger.info(logEntry);
            
        } catch (IOException e) {
            logger.error("Error al escribir en archivo de log", e);
        }
    }
    
    @Hook(Pointcut.SERVER_OUTGOING_RESPONSE)
    public void logResponse(RequestDetails requestDetails, Integer statusCode) {
        if (requestDetails == null) {
            logger.warn("RequestDetails es null en logResponse");
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String path = requestDetails.getRequestPath();
            String username = (String) requestDetails.getAttribute("USERNAME");
            
            String logEntry = String.format(
                "[%s] RESPUESTA | PATH=%s | STATUS=%d | USUARIO=%s",
                timestamp,
                path,
                statusCode,
                username != null ? username : "ANONYMOUS"
            );
            
            // Escribir en archivo
            try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
                writer.println(logEntry);
            }
            
            logger.info(logEntry);
            
        } catch (IOException e) {
            logger.error("Error al escribir en archivo de log", e);
        }
    }
}

