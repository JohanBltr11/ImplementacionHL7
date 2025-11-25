package com.example.fhir.provider;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.example.fhir.model.User;
import com.example.fhir.storage.InMemoryStorage;
import com.example.fhir.util.AuthContext;
import com.example.fhir.util.ValidationUtil;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;

import java.util.List;

/**
 * Proveedor de recursos Patient
 * Implementa operaciones CRUD básicas con almacenamiento en memoria
 */
public class PatientProvider implements IResourceProvider {
    
    private final InMemoryStorage storage = InMemoryStorage.getInstance();
    
    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }
    
    /**
     * Crear un nuevo Patient
     * POST /fhir/Patient
     * Solo administradores pueden crear Patients
     */
    @Create
    public MethodOutcome createPatient(@ResourceParam Patient patient, RequestDetails requestDetails) {
        // Verificar permisos - solo admin puede crear
        User user = getUserFromRequest(requestDetails);
        if (!user.isAdmin()) {
            throw new ForbiddenOperationException("Solo los administradores pueden crear recursos Patient");
        }
        
        // Validar recurso
        ValidationUtil.validateResource(patient);
        
        // Usar ID del recurso si existe, o generar uno nuevo
        String finalId;
        if (patient.getIdElement().isEmpty()) {
            // Generar ID único
            do {
                finalId = "Patient-" + System.currentTimeMillis();
            } while (storage.getPatient(finalId) != null);
            patient.setId(finalId);
        } else {
            // Si el recurso ya tiene un ID, extraer solo la parte del ID (sin el tipo de recurso)
            String idPart = patient.getIdElement().getIdPart();
            // Si el ID incluye el tipo de recurso (ej: "Patient/123"), extraer solo "123"
            if (idPart.contains("/")) {
                idPart = idPart.substring(idPart.lastIndexOf("/") + 1);
            }
            finalId = idPart;
            
            // Verificar que el ID no exista ya
            if (storage.getPatient(finalId) != null) {
                throw new ca.uhn.fhir.rest.server.exceptions.InvalidRequestException(
                    "Ya existe un Patient con el ID '" + finalId + "'. Por favor, use un ID diferente."
                );
            }
            
            patient.setId("Patient/" + finalId);
        }
        
        // Guardar
        storage.savePatient(patient);
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(patient.getIdElement());
        outcome.setResource(patient);
        outcome.setCreated(true);
        
        return outcome;
    }
    
    /**
     * Leer un Patient por ID
     * GET /fhir/Patient/{id}
     * Usuarios regulares solo pueden leer su propio Patient
     */
    @Read
    public Patient readPatient(@IdParam IdType id, RequestDetails requestDetails) {
        Patient patient = storage.getPatient(id.getIdPart());
        if (patient == null) {
            throw new ResourceNotFoundException("Patient con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        // Verificar permisos de acceso
        User user = getUserFromRequest(requestDetails);
        if (!user.isAdmin() && !id.getIdPart().equals(user.getPatientId())) {
            throw new ForbiddenOperationException("No tiene permisos para acceder a este recurso Patient");
        }
        
        return patient;
    }
    
    /**
     * Actualizar un Patient
     * PUT /fhir/Patient/{id}
     * Solo administradores pueden actualizar
     */
    @Update
    public MethodOutcome updatePatient(@IdParam IdType id, @ResourceParam Patient patient, RequestDetails requestDetails) {
        // Verificar permisos - solo admin puede actualizar
        User user = getUserFromRequest(requestDetails);
        if (!user.isAdmin()) {
            throw new ForbiddenOperationException("Solo los administradores pueden actualizar recursos Patient");
        }
        
        // Validar recurso
        ValidationUtil.validateResource(patient);
        
        // Verificar que existe
        Patient existing = storage.getPatient(id.getIdPart());
        if (existing == null) {
            throw new ResourceNotFoundException("Patient con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        // Verificar si el recurso tiene un ID diferente al parámetro
        String resourceId = patient.getIdElement().getIdPart();
        if (resourceId.contains("/")) {
            resourceId = resourceId.substring(resourceId.lastIndexOf("/") + 1);
        }
        
        // Si el ID del recurso es diferente al ID del parámetro, verificar que no exista ya
        if (!resourceId.equals(id.getIdPart()) && storage.getPatient(resourceId) != null) {
            throw new InvalidRequestException(
                "Ya existe un Patient con el ID '" + resourceId + "'. No se puede cambiar el ID a uno que ya existe."
            );
        }
        
        // Actualizar ID si es necesario
        patient.setId("Patient/" + id.getIdPart());
        
        // Guardar
        storage.savePatient(patient);
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(patient.getIdElement());
        outcome.setResource(patient);
        
        return outcome;
    }
    
    /**
     * Eliminar un Patient
     * DELETE /fhir/Patient/{id}
     * Solo administradores pueden eliminar
     */
    @Delete
    public MethodOutcome deletePatient(@IdParam IdType id, RequestDetails requestDetails) {
        // Verificar permisos - solo admin puede eliminar
        User user = getUserFromRequest(requestDetails);
        if (!user.isAdmin()) {
            throw new ForbiddenOperationException("Solo los administradores pueden eliminar recursos Patient");
        }
        
        Patient patient = storage.getPatient(id.getIdPart());
        if (patient == null) {
            throw new ResourceNotFoundException("Patient con ID '" + id.getIdPart() + "' no encontrado");
        }
        
        storage.deletePatient(id.getIdPart());
        
        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(id);
        
        return outcome;
    }
    
    /**
     * Buscar todos los Patients
     * GET /fhir/Patient
     * Usuarios regulares solo ven su propio Patient
     */
    @Search
    public List<Patient> searchPatients(RequestDetails requestDetails) {
        User user = getUserFromRequest(requestDetails);
        
        // Administradores ven todos los Patients
        if (user.isAdmin()) {
            return storage.getAllPatients();
        }
        
        // Usuarios regulares solo ven su propio Patient
        if (user.getPatientId() != null && !user.getPatientId().isEmpty()) {
            Patient patient = storage.getPatient(user.getPatientId());
            if (patient != null) {
                return List.of(patient);
            }
        }
        
        // Si el usuario no tiene Patient asociado, retorna lista vacía
        return List.of();
    }
    
    /**
     * Obtener el usuario autenticado desde RequestDetails o ThreadLocal
     */
    private User getUserFromRequest(RequestDetails requestDetails) {
        System.out.println("PatientProvider: ===== INICIO getUserFromRequest =====");
        System.out.println("PatientProvider: Thread: " + Thread.currentThread().getName());
        
        // Primero intentar obtener de ThreadLocal (más confiable)
        User user = AuthContext.getUser();
        System.out.println("PatientProvider: Usuario desde ThreadLocal: " + (user != null ? user.getUsername() : "NULL"));
        
        // Si no está en ThreadLocal, intentar desde RequestDetails
        if (user == null && requestDetails != null) {
            user = (User) requestDetails.getAttribute("USER");
            System.out.println("PatientProvider: Usuario desde RequestDetails: " + (user != null ? user.getUsername() : "NULL"));
        }
        
        if (user == null) {
            System.err.println("PatientProvider: Usuario no encontrado ni en ThreadLocal ni en RequestDetails");
            System.err.println("PatientProvider: Thread actual: " + Thread.currentThread().getName());
            if (requestDetails != null) {
                String username = (String) requestDetails.getAttribute("USERNAME");
                String role = (String) requestDetails.getAttribute("ROLE");
                System.err.println("PatientProvider: USERNAME attribute = " + username);
                System.err.println("PatientProvider: ROLE attribute = " + role);
                System.err.println("PatientProvider: RequestPath = " + requestDetails.getRequestPath());
            }
            throw new ForbiddenOperationException("Usuario no autenticado. El SecurityInterceptor no se ejecutó correctamente.");
        }
        
        System.out.println("PatientProvider: Usuario obtenido exitosamente: " + user.getUsername() + " (rol: " + user.getRole() + ")");
        return user;
    }
}

