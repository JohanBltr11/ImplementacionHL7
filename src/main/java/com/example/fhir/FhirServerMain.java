package com.example.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import com.example.fhir.interceptor.LoggingInterceptor;
import com.example.fhir.interceptor.SecurityInterceptor;
import com.example.fhir.provider.ObservationProvider;
import com.example.fhir.provider.PatientProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import jakarta.servlet.ServletException;

/**
 * Servidor FHIR embebido usando HAPI FHIR y Jetty
 * 
 * Para ejecutar:
 * 1. Compilar: mvn clean install
 * 2. Ejecutar: mvn exec:java
 *    O directamente: java -cp target/implementacion-hl7-1.0-SNAPSHOT.jar com.example.fhir.FhirServerMain
 * 
 * El servidor estará disponible en: http://localhost:8080/fhir/
 * 
 * Endpoints disponibles:
 * - GET  /fhir/Patient/{id} - Obtener paciente
 * - POST /fhir/Patient - Crear paciente
 * - PUT  /fhir/Patient/{id} - Actualizar paciente
 * - DELETE /fhir/Patient/{id} - Eliminar paciente
 * - GET  /fhir/Observation/{id} - Obtener observación
 * - POST /fhir/Observation - Crear observación
 * - PUT  /fhir/Observation/{id} - Actualizar observación
 * - DELETE /fhir/Observation/{id} - Eliminar observación
 * 
 * Autenticación:
 * - Usuario admin: admin / admin123 (permisos completos)
 * - Usuario user: user / user123 (solo lectura)
 */
public class FhirServerMain extends RestfulServer {
    
    private static final int PORT = 8080;
    private static final FhirContext FHIR_CONTEXT = FhirContext.forR4();
    
    @Override
    protected void initialize() throws ServletException {
        // Configurar el contexto FHIR
        setFhirContext(FHIR_CONTEXT);
        
        // Registrar proveedores de recursos
        registerProvider(new PatientProvider());
        registerProvider(new ObservationProvider());
        
        // Interceptores (el orden importa: Security debe ir primero)
        registerInterceptor(new SecurityInterceptor());
        registerInterceptor(new LoggingInterceptor());
        registerInterceptor(new ResponseHighlighterInterceptor());
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("==========================================");
        System.out.println("Iniciando Servidor FHIR HAPI");
        System.out.println("==========================================");
        System.out.println("Puerto: " + PORT);
        System.out.println("Base URL: http://localhost:" + PORT + "/fhir/");
        System.out.println("==========================================");
        
        // Crear servidor Jetty
        Server server = new Server(PORT);
        
        // Configurar contexto
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        
        // Registrar servlet FHIR
        FhirServerMain servlet = new FhirServerMain();
        ServletHolder servletHolder = new ServletHolder(servlet);
        context.addServlet(servletHolder, "/fhir/*");
        
        // Iniciar servidor
        server.start();
        System.out.println("Servidor FHIR iniciado correctamente!");
        System.out.println("Presiona Ctrl+C para detener el servidor.");
        
        server.join();
    }
}

